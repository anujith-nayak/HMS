import { useEffect, useState, useCallback } from 'react'
import toast from 'react-hot-toast'
import { getDoctors, createDoctor, updateDoctor, deleteDoctor } from '../api/doctors'
import Modal from '../components/Modal'
import ConfirmDialog from '../components/ConfirmDialog'
import Spinner from '../components/Spinner'
import SearchBar from '../components/SearchBar'
import EmptyState from '../components/EmptyState'

const EMPTY = { fullName: '', specialization: '', department: '', phoneNumber: '', email: '' }

export default function Doctors() {
  const [doctors, setDoctors] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [modal, setModal] = useState({ open: false, mode: 'add', data: null })
  const [confirm, setConfirm] = useState({ open: false, id: null })
  const [form, setForm] = useState(EMPTY)
  const [errors, setErrors] = useState({})
  const [saving, setSaving] = useState(false)

  const load = useCallback((q = '') => {
    setLoading(true)
    getDoctors(q).then(r => setDoctors(r.data)).catch(() => toast.error('Failed to load doctors')).finally(() => setLoading(false))
  }, [])

  useEffect(() => { load() }, [load])
  useEffect(() => {
    const t = setTimeout(() => load(search), 300)
    return () => clearTimeout(t)
  }, [search, load])

  const openAdd = () => { setForm(EMPTY); setErrors({}); setModal({ open: true, mode: 'add' }) }
  const openEdit = (d) => { setForm({ ...d }); setErrors({}); setModal({ open: true, mode: 'edit', data: d }) }

  const validate = () => {
    const e = {}
    if (!form.fullName.trim()) e.fullName = 'Full name is required'
    if (!form.specialization.trim()) e.specialization = 'Specialization is required'
    if (!form.department.trim()) e.department = 'Department is required'
    if (!form.phoneNumber.trim()) e.phoneNumber = 'Phone is required'
    if (!form.email.trim()) e.email = 'Email is required'
    else if (!/\S+@\S+\.\S+/.test(form.email)) e.email = 'Invalid email'
    return e
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length) { setErrors(errs); return }
    setSaving(true)
    try {
      if (modal.mode === 'add') {
        await createDoctor(form); toast.success('Doctor added successfully')
      } else {
        await updateDoctor(modal.data.doctorId, form); toast.success('Doctor updated successfully')
      }
      setModal({ open: false }); load(search)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Operation failed')
    } finally { setSaving(false) }
  }

  const handleDelete = async () => {
    try { await deleteDoctor(confirm.id); toast.success('Doctor deleted'); load(search) }
    catch { toast.error('Delete failed') }
    finally { setConfirm({ open: false }) }
  }

  const specializations = ['Cardiology','Neurology','Orthopedics','Pediatrics','Dermatology','Gynecology','Oncology','Radiology','Surgery','General Medicine','ENT','Ophthalmology','Psychiatry','Urology']

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <SearchBar value={search} onChange={setSearch} placeholder="Search doctors…" />
        <button onClick={openAdd} className="btn-primary">
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          Add Doctor
        </button>
      </div>

      <div className="card p-0 overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-16"><Spinner size="lg" /></div>
        ) : doctors.length === 0 ? (
          <EmptyState icon="👨‍⚕️" title="No doctors found" subtitle="Add a doctor to get started." action={<button onClick={openAdd} className="btn-primary">Add Doctor</button>} />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-slate-50 border-b border-slate-200">
                <tr>
                  <th className="table-header">#</th>
                  <th className="table-header">Full Name</th>
                  <th className="table-header">Specialization</th>
                  <th className="table-header">Department</th>
                  <th className="table-header">Phone</th>
                  <th className="table-header">Email</th>
                  <th className="table-header">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {doctors.map((d, i) => (
                  <tr key={d.doctorId} className="hover:bg-slate-50 transition-colors">
                    <td className="table-cell text-slate-400 text-xs">{i + 1}</td>
                    <td className="table-cell font-medium text-slate-800">Dr. {d.fullName}</td>
                    <td className="table-cell"><span className="badge bg-teal-100 text-teal-700">{d.specialization}</span></td>
                    <td className="table-cell">{d.department}</td>
                    <td className="table-cell">{d.phoneNumber}</td>
                    <td className="table-cell text-xs text-slate-500">{d.email}</td>
                    <td className="table-cell">
                      <div className="flex gap-2">
                        <button onClick={() => openEdit(d)} className="btn-edit">Edit</button>
                        <button onClick={() => setConfirm({ open: true, id: d.doctorId })} className="btn-danger">Delete</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <Modal open={modal.open} onClose={() => setModal({ open: false })} title={modal.mode === 'add' ? 'Add Doctor' : 'Edit Doctor'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="sm:col-span-2">
              <label className="form-label">Full Name *</label>
              <input className={errors.fullName ? 'form-input-error' : 'form-input'} value={form.fullName}
                onChange={e => setForm(f => ({ ...f, fullName: e.target.value }))} placeholder="e.g. Suresh Patel" />
              {errors.fullName && <p className="text-xs text-red-500 mt-1">{errors.fullName}</p>}
            </div>
            <div>
              <label className="form-label">Specialization *</label>
              <select className={errors.specialization ? 'form-input-error' : 'form-input'} value={form.specialization}
                onChange={e => setForm(f => ({ ...f, specialization: e.target.value }))}>
                <option value="">Select specialization</option>
                {specializations.map(s => <option key={s}>{s}</option>)}
              </select>
              {errors.specialization && <p className="text-xs text-red-500 mt-1">{errors.specialization}</p>}
            </div>
            <div>
              <label className="form-label">Department *</label>
              <input className={errors.department ? 'form-input-error' : 'form-input'} value={form.department}
                onChange={e => setForm(f => ({ ...f, department: e.target.value }))} placeholder="e.g. ICU" />
              {errors.department && <p className="text-xs text-red-500 mt-1">{errors.department}</p>}
            </div>
            <div>
              <label className="form-label">Phone *</label>
              <input className={errors.phoneNumber ? 'form-input-error' : 'form-input'} value={form.phoneNumber}
                onChange={e => setForm(f => ({ ...f, phoneNumber: e.target.value }))} placeholder="+91 99999 00000" />
              {errors.phoneNumber && <p className="text-xs text-red-500 mt-1">{errors.phoneNumber}</p>}
            </div>
            <div>
              <label className="form-label">Email *</label>
              <input type="email" className={errors.email ? 'form-input-error' : 'form-input'} value={form.email}
                onChange={e => setForm(f => ({ ...f, email: e.target.value }))} placeholder="doctor@hospital.com" />
              {errors.email && <p className="text-xs text-red-500 mt-1">{errors.email}</p>}
            </div>
          </div>
          <div className="flex gap-3 justify-end pt-2 border-t border-slate-100">
            <button type="button" onClick={() => setModal({ open: false })} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={saving} className="btn-primary">
              {saving && <Spinner size="sm" />}
              {modal.mode === 'add' ? 'Add Doctor' : 'Save Changes'}
            </button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog open={confirm.open} onConfirm={handleDelete} onCancel={() => setConfirm({ open: false })} />
    </div>
  )
}

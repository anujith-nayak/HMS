import { useEffect, useState, useCallback } from 'react'
import toast from 'react-hot-toast'
import { getPrescriptions, createPrescription, updatePrescription, deletePrescription } from '../api/prescriptions'
import { getPatients } from '../api/patients'
import { getDoctors } from '../api/doctors'
import Modal from '../components/Modal'
import ConfirmDialog from '../components/ConfirmDialog'
import Spinner from '../components/Spinner'
import SearchBar from '../components/SearchBar'
import EmptyState from '../components/EmptyState'

const EMPTY = { patientId: '', doctorId: '', medicineName: '', dosage: '', instructions: '', prescribedDate: '' }

export default function Prescriptions() {
  const [prescriptions, setPrescriptions] = useState([])
  const [patients, setPatients] = useState([])
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
    getPrescriptions(q)
      .then(r => setPrescriptions(r.data))
      .catch(() => toast.error('Failed to load prescriptions'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    load()
    getPatients().then(r => setPatients(r.data)).catch(() => {})
    getDoctors().then(r => setDoctors(r.data)).catch(() => {})
  }, [load])

  useEffect(() => {
    const t = setTimeout(() => load(search), 300)
    return () => clearTimeout(t)
  }, [search, load])

  const openAdd = () => { setForm(EMPTY); setErrors({}); setModal({ open: true, mode: 'add' }) }
  const openEdit = (p) => {
    setForm({
      patientId: p.patientId,
      doctorId: p.doctorId,
      medicineName: p.medicineName,
      dosage: p.dosage,
      instructions: p.instructions || '',
      prescribedDate: p.prescribedDate,
    })
    setErrors({})
    setModal({ open: true, mode: 'edit', data: p })
  }

  const validate = () => {
    const e = {}
    if (!form.patientId) e.patientId = 'Patient is required'
    if (!form.doctorId) e.doctorId = 'Doctor is required'
    if (!form.medicineName.trim()) e.medicineName = 'Medicine name is required'
    if (!form.dosage.trim()) e.dosage = 'Dosage is required'
    if (!form.prescribedDate) e.prescribedDate = 'Date is required'
    return e
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length) { setErrors(errs); return }
    setSaving(true)
    const payload = { ...form, patientId: Number(form.patientId), doctorId: Number(form.doctorId) }
    try {
      if (modal.mode === 'add') {
        await createPrescription(payload); toast.success('Prescription added')
      } else {
        await updatePrescription(modal.data.prescriptionId, payload); toast.success('Prescription updated')
      }
      setModal({ open: false }); load(search)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Operation failed')
    } finally { setSaving(false) }
  }

  const handleDelete = async () => {
    try { await deletePrescription(confirm.id); toast.success('Prescription deleted'); load(search) }
    catch { toast.error('Delete failed') }
    finally { setConfirm({ open: false }) }
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <SearchBar value={search} onChange={setSearch} placeholder="Search by patient, doctor, medicine…" />
        <button onClick={openAdd} className="btn-primary">
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          Add Prescription
        </button>
      </div>

      <div className="card p-0 overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-16"><Spinner size="lg" /></div>
        ) : prescriptions.length === 0 ? (
          <EmptyState icon="💊" title="No prescriptions found" subtitle="Add a prescription to get started."
            action={<button onClick={openAdd} className="btn-primary">Add Prescription</button>} />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-slate-50 border-b border-slate-200">
                <tr>
                  <th className="table-header">#</th>
                  <th className="table-header">Patient</th>
                  <th className="table-header">Doctor</th>
                  <th className="table-header">Medicine</th>
                  <th className="table-header">Dosage</th>
                  <th className="table-header">Instructions</th>
                  <th className="table-header">Date</th>
                  <th className="table-header">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {prescriptions.map((p, i) => (
                  <tr key={p.prescriptionId} className="hover:bg-slate-50 transition-colors">
                    <td className="table-cell text-slate-400 text-xs">{i + 1}</td>
                    <td className="table-cell font-medium text-slate-800">{p.patientName}</td>
                    <td className="table-cell text-slate-600">Dr. {p.doctorName}</td>
                    <td className="table-cell">
                      <span className="badge bg-rose-100 text-rose-700">{p.medicineName}</span>
                    </td>
                    <td className="table-cell">{p.dosage}</td>
                    <td className="table-cell max-w-xs truncate text-slate-500 text-xs">{p.instructions || '—'}</td>
                    <td className="table-cell">{p.prescribedDate}</td>
                    <td className="table-cell">
                      <div className="flex gap-2">
                        <button onClick={() => openEdit(p)} className="btn-edit">Edit</button>
                        <button onClick={() => setConfirm({ open: true, id: p.prescriptionId })} className="btn-danger">Delete</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <Modal open={modal.open} onClose={() => setModal({ open: false })}
        title={modal.mode === 'add' ? 'Add Prescription' : 'Edit Prescription'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="form-label">Patient *</label>
              <select className={errors.patientId ? 'form-input-error' : 'form-input'} value={form.patientId}
                onChange={e => setForm(f => ({ ...f, patientId: e.target.value }))}>
                <option value="">Select patient</option>
                {patients.map(p => <option key={p.patientId} value={p.patientId}>{p.fullName}</option>)}
              </select>
              {errors.patientId && <p className="text-xs text-red-500 mt-1">{errors.patientId}</p>}
            </div>
            <div>
              <label className="form-label">Doctor *</label>
              <select className={errors.doctorId ? 'form-input-error' : 'form-input'} value={form.doctorId}
                onChange={e => setForm(f => ({ ...f, doctorId: e.target.value }))}>
                <option value="">Select doctor</option>
                {doctors.map(d => <option key={d.doctorId} value={d.doctorId}>Dr. {d.fullName}</option>)}
              </select>
              {errors.doctorId && <p className="text-xs text-red-500 mt-1">{errors.doctorId}</p>}
            </div>
            <div>
              <label className="form-label">Medicine Name *</label>
              <input className={errors.medicineName ? 'form-input-error' : 'form-input'} value={form.medicineName}
                onChange={e => setForm(f => ({ ...f, medicineName: e.target.value }))} placeholder="e.g. Paracetamol 500mg" />
              {errors.medicineName && <p className="text-xs text-red-500 mt-1">{errors.medicineName}</p>}
            </div>
            <div>
              <label className="form-label">Dosage *</label>
              <input className={errors.dosage ? 'form-input-error' : 'form-input'} value={form.dosage}
                onChange={e => setForm(f => ({ ...f, dosage: e.target.value }))} placeholder="e.g. 1 tablet twice daily" />
              {errors.dosage && <p className="text-xs text-red-500 mt-1">{errors.dosage}</p>}
            </div>
            <div>
              <label className="form-label">Prescribed Date *</label>
              <input type="date" className={errors.prescribedDate ? 'form-input-error' : 'form-input'}
                value={form.prescribedDate} max={new Date().toISOString().split('T')[0]}
                onChange={e => setForm(f => ({ ...f, prescribedDate: e.target.value }))} />
              {errors.prescribedDate && <p className="text-xs text-red-500 mt-1">{errors.prescribedDate}</p>}
            </div>
            <div className="sm:col-span-2">
              <label className="form-label">Instructions</label>
              <textarea className="form-input" rows={2} value={form.instructions}
                onChange={e => setForm(f => ({ ...f, instructions: e.target.value }))}
                placeholder="e.g. Take after meals, avoid alcohol" />
            </div>
          </div>
          <div className="flex gap-3 justify-end pt-2 border-t border-slate-100">
            <button type="button" onClick={() => setModal({ open: false })} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={saving} className="btn-primary">
              {saving && <Spinner size="sm" />}
              {modal.mode === 'add' ? 'Add Prescription' : 'Save Changes'}
            </button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog open={confirm.open} onConfirm={handleDelete} onCancel={() => setConfirm({ open: false })} />
    </div>
  )
}

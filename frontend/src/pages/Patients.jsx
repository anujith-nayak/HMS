import { useEffect, useState, useCallback } from 'react'
import toast from 'react-hot-toast'
import { getPatients, createPatient, updatePatient, deletePatient, getPatientHistory } from '../api/patients'
import Modal from '../components/Modal'
import ConfirmDialog from '../components/ConfirmDialog'
import Spinner from '../components/Spinner'
import SearchBar from '../components/SearchBar'
import EmptyState from '../components/EmptyState'

const EMPTY = { fullName: '', gender: '', dateOfBirth: '', phoneNumber: '', address: '', abhaNumber: '' }

export default function Patients() {
  const [patients, setPatients] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [modal, setModal] = useState({ open: false, mode: 'add', data: null })
  const [confirm, setConfirm] = useState({ open: false, id: null })
  const [form, setForm] = useState(EMPTY)
  const [errors, setErrors] = useState({})
  const [saving, setSaving] = useState(false)
  const [historyModal, setHistoryModal] = useState({ open: false, patient: null, data: null, loading: false })

  const load = useCallback((q = '') => {
    setLoading(true)
    getPatients(q).then(r => setPatients(r.data)).catch(() => toast.error('Failed to load patients')).finally(() => setLoading(false))
  }, [])

  useEffect(() => { load() }, [load])
  useEffect(() => {
    const t = setTimeout(() => load(search), 300)
    return () => clearTimeout(t)
  }, [search, load])

  const openAdd = () => { setForm(EMPTY); setErrors({}); setModal({ open: true, mode: 'add' }) }
  const openEdit = (p) => {
    setForm({ ...p, dateOfBirth: p.dateOfBirth || '', abhaNumber: p.abhaNumber || '' })
    setErrors({})
    setModal({ open: true, mode: 'edit', data: p })
  }

  const validate = () => {
    const e = {}
    if (!form.fullName.trim()) e.fullName = 'Full name is required'
    if (!form.gender) e.gender = 'Gender is required'
    if (!form.dateOfBirth) e.dateOfBirth = 'Date of birth is required'
    if (!form.phoneNumber.trim()) e.phoneNumber = 'Phone number is required'
    if (!form.address.trim()) e.address = 'Address is required'
    return e
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length) { setErrors(errs); return }
    setSaving(true)
    try {
      if (modal.mode === 'add') {
        await createPatient(form)
        toast.success('Patient added successfully')
      } else {
        await updatePatient(modal.data.patientId, form)
        toast.success('Patient updated successfully')
      }
      setModal({ open: false })
      load(search)
    } catch (err) {
      const data = err.response?.data
      const msg = data?.message || (data?.fieldErrors ? JSON.stringify(data.fieldErrors) : null) || 'Operation failed'
      toast.error(msg)
      console.error('Patient save error:', data)
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async () => {
    try {
      await deletePatient(confirm.id)
      toast.success('Patient deleted')
      load(search)
    } catch { toast.error('Delete failed') }
    finally { setConfirm({ open: false }) }
  }

  const openHistory = async (patient) => {
    setHistoryModal({ open: true, patient, data: null, loading: true })
    try {
      const res = await getPatientHistory(patient.patientId)
      setHistoryModal({ open: true, patient, data: res.data, loading: false })
    } catch {
      toast.error('Failed to load patient history')
      setHistoryModal({ open: true, patient, data: null, loading: false })
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <SearchBar value={search} onChange={setSearch} placeholder="Search patients…" />
        <button onClick={openAdd} className="btn-primary">
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          Add Patient
        </button>
      </div>

      <div className="card p-0 overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-16"><Spinner size="lg" /></div>
        ) : patients.length === 0 ? (
          <EmptyState icon="🧑‍⚕️" title="No patients found" subtitle="Add a new patient to get started." action={<button onClick={openAdd} className="btn-primary">Add Patient</button>} />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-slate-50 border-b border-slate-200">
                <tr>
                  <th className="table-header">#</th>
                  <th className="table-header">Full Name</th>
                  <th className="table-header">Gender</th>
                  <th className="table-header">Date of Birth</th>
                  <th className="table-header">Phone</th>
                  <th className="table-header">ABHA No.</th>
                  <th className="table-header">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {patients.map((p, i) => (
                  <tr key={p.patientId} className="hover:bg-slate-50 transition-colors">
                    <td className="table-cell text-slate-400 font-mono text-xs">{i + 1}</td>
                    <td className="table-cell font-medium text-slate-800">{p.fullName}</td>
                    <td className="table-cell">
                      <span className={`badge ${p.gender === 'Male' ? 'bg-blue-100 text-blue-700' : p.gender === 'Female' ? 'bg-pink-100 text-pink-700' : 'bg-slate-100 text-slate-700'}`}>
                        {p.gender}
                      </span>
                    </td>
                    <td className="table-cell">{p.dateOfBirth}</td>
                    <td className="table-cell">{p.phoneNumber}</td>
                    <td className="table-cell font-mono text-xs">{p.abhaNumber || '—'}</td>
                    <td className="table-cell">
                      <div className="flex gap-2">
                        <button onClick={() => openHistory(p)} className="btn-secondary">History</button>
                        <button onClick={() => openEdit(p)} className="btn-edit">Edit</button>
                        <button onClick={() => setConfirm({ open: true, id: p.patientId })} className="btn-danger">Delete</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <Modal open={modal.open} onClose={() => setModal({ open: false })} title={modal.mode === 'add' ? 'Add Patient' : 'Edit Patient'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="sm:col-span-2">
              <label className="form-label">Full Name *</label>
              <input className={errors.fullName ? 'form-input-error' : 'form-input'} value={form.fullName}
                onChange={e => setForm(f => ({ ...f, fullName: e.target.value }))} placeholder="e.g. Ramesh Kumar" />
              {errors.fullName && <p className="text-xs text-red-500 mt-1">{errors.fullName}</p>}
            </div>
            <div>
              <label className="form-label">Gender *</label>
              <select className={errors.gender ? 'form-input-error' : 'form-input'} value={form.gender}
                onChange={e => setForm(f => ({ ...f, gender: e.target.value }))}>
                <option value="">Select gender</option>
                <option>Male</option><option>Female</option><option>Other</option>
              </select>
              {errors.gender && <p className="text-xs text-red-500 mt-1">{errors.gender}</p>}
            </div>
            <div>
              <label className="form-label">Date of Birth *</label>
              <input type="date" className={errors.dateOfBirth ? 'form-input-error' : 'form-input'} value={form.dateOfBirth}
                onChange={e => setForm(f => ({ ...f, dateOfBirth: e.target.value }))} max={new Date().toISOString().split('T')[0]} />
              {errors.dateOfBirth && <p className="text-xs text-red-500 mt-1">{errors.dateOfBirth}</p>}
            </div>
            <div>
              <label className="form-label">Phone Number *</label>
              <input className={errors.phoneNumber ? 'form-input-error' : 'form-input'} value={form.phoneNumber}
                onChange={e => setForm(f => ({ ...f, phoneNumber: e.target.value }))} placeholder="+91 98765 43210" />
              {errors.phoneNumber && <p className="text-xs text-red-500 mt-1">{errors.phoneNumber}</p>}
            </div>
            <div>
              <label className="form-label">ABHA Number</label>
              <input className="form-input" value={form.abhaNumber}
                onChange={e => setForm(f => ({ ...f, abhaNumber: e.target.value }))} placeholder="Optional" />
            </div>
            <div className="sm:col-span-2">
              <label className="form-label">Address *</label>
              <textarea className={errors.address ? 'form-input-error' : 'form-input'} rows={2} value={form.address}
                onChange={e => setForm(f => ({ ...f, address: e.target.value }))} placeholder="Full address" />
              {errors.address && <p className="text-xs text-red-500 mt-1">{errors.address}</p>}
            </div>
          </div>
          <div className="flex gap-3 justify-end pt-2 border-t border-slate-100">
            <button type="button" onClick={() => setModal({ open: false })} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={saving} className="btn-primary">
              {saving ? <Spinner size="sm" /> : null}
              {modal.mode === 'add' ? 'Add Patient' : 'Save Changes'}
            </button>
          </div>
        </form>
      </Modal>

      <Modal open={historyModal.open} onClose={() => setHistoryModal({ open: false, patient: null, data: null, loading: false })} title={historyModal.patient ? `${historyModal.patient.fullName} - History` : 'Patient History'}>
        {historyModal.loading ? (
          <div className="flex justify-center py-8"><Spinner size="md" /></div>
        ) : historyModal.data ? (
          <pre className="text-sm whitespace-pre-wrap bg-slate-50 p-4 rounded-lg">{JSON.stringify(historyModal.data, null, 2)}</pre>
        ) : (
          <p className="text-sm text-slate-500">No history returned from EHRbase.</p>
        )}
      </Modal>

      <ConfirmDialog open={confirm.open} onConfirm={handleDelete} onCancel={() => setConfirm({ open: false })} />
    </div>
  )
}

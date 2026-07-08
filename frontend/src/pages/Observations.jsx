import { useEffect, useState, useCallback } from 'react'
import toast from 'react-hot-toast'
import { getObservations, createObservation, updateObservation, deleteObservation } from '../api/observations'
import { getPatients } from '../api/patients'
import Modal from '../components/Modal'
import ConfirmDialog from '../components/ConfirmDialog'
import Spinner from '../components/Spinner'
import SearchBar from '../components/SearchBar'
import EmptyState from '../components/EmptyState'

const EMPTY = {
  patientId: '', bloodPressure: '', temperature: '', heartRate: '',
  weight: '', height: '', recordedDate: '',
}

export default function Observations() {
  const [observations, setObservations] = useState([])
  const [patients, setPatients] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [modal, setModal] = useState({ open: false, mode: 'add', data: null })
  const [confirm, setConfirm] = useState({ open: false, id: null })
  const [form, setForm] = useState(EMPTY)
  const [errors, setErrors] = useState({})
  const [saving, setSaving] = useState(false)

  const load = useCallback((q = '') => {
    setLoading(true)
    getObservations(q)
      .then(r => setObservations(r.data))
      .catch(() => toast.error('Failed to load observations'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    load()
    getPatients().then(r => setPatients(r.data)).catch(() => {})
  }, [load])

  useEffect(() => {
    const t = setTimeout(() => load(search), 300)
    return () => clearTimeout(t)
  }, [search, load])

  const openAdd = () => { setForm(EMPTY); setErrors({}); setModal({ open: true, mode: 'add' }) }
  const openEdit = (o) => {
    setForm({
      patientId: o.patientId,
      bloodPressure: o.bloodPressure,
      temperature: o.temperature,
      heartRate: o.heartRate,
      weight: o.weight,
      height: o.height,
      recordedDate: o.recordedDate,
    })
    setErrors({})
    setModal({ open: true, mode: 'edit', data: o })
  }

  const validate = () => {
    const e = {}
    if (!form.patientId) e.patientId = 'Patient is required'
    if (!form.bloodPressure.trim()) e.bloodPressure = 'Blood pressure is required'
    if (!form.temperature) e.temperature = 'Temperature is required'
    if (!form.heartRate) e.heartRate = 'Heart rate is required'
    if (!form.weight) e.weight = 'Weight is required'
    if (!form.height) e.height = 'Height is required'
    if (!form.recordedDate) e.recordedDate = 'Date is required'
    return e
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length) { setErrors(errs); return }
    setSaving(true)
    const payload = {
      ...form,
      patientId: Number(form.patientId),
      temperature: parseFloat(form.temperature),
      heartRate: parseInt(form.heartRate),
      weight: parseFloat(form.weight),
      height: parseFloat(form.height),
    }
    try {
      if (modal.mode === 'add') {
        await createObservation(payload); toast.success('Observation recorded')
      } else {
        await updateObservation(modal.data.observationId, payload); toast.success('Observation updated')
      }
      setModal({ open: false }); load(search)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Operation failed')
    } finally { setSaving(false) }
  }

  const handleDelete = async () => {
    try { await deleteObservation(confirm.id); toast.success('Observation deleted'); load(search) }
    catch { toast.error('Delete failed') }
    finally { setConfirm({ open: false }) }
  }

  // Helper to flag abnormal vitals
  const bpClass = (bp) => {
    // crude check — just highlight if looks high
    const sys = parseInt(bp?.split('/')?.[0])
    if (!sys) return ''
    return sys >= 140 ? 'text-red-600 font-medium' : sys >= 120 ? 'text-yellow-600' : 'text-green-600'
  }
  const hrClass = (hr) => {
    if (hr > 100 || hr < 60) return 'text-red-600 font-medium'
    return 'text-green-600'
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <SearchBar value={search} onChange={setSearch} placeholder="Search by patient name…" />
        <button onClick={openAdd} className="btn-primary">
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          Record Vitals
        </button>
      </div>

      <div className="card p-0 overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-16"><Spinner size="lg" /></div>
        ) : observations.length === 0 ? (
          <EmptyState icon="🩺" title="No observations found" subtitle="Record patient vitals to get started."
            action={<button onClick={openAdd} className="btn-primary">Record Vitals</button>} />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-slate-50 border-b border-slate-200">
                <tr>
                  <th className="table-header">#</th>
                  <th className="table-header">Patient</th>
                  <th className="table-header">Blood Pressure</th>
                  <th className="table-header">Temp (°C)</th>
                  <th className="table-header">Heart Rate</th>
                  <th className="table-header">Weight (kg)</th>
                  <th className="table-header">Height (cm)</th>
                  <th className="table-header">Date</th>
                  <th className="table-header">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {observations.map((o, i) => (
                  <tr key={o.observationId} className="hover:bg-slate-50 transition-colors">
                    <td className="table-cell text-slate-400 text-xs">{i + 1}</td>
                    <td className="table-cell font-medium text-slate-800">{o.patientName}</td>
                    <td className={`table-cell ${bpClass(o.bloodPressure)}`}>{o.bloodPressure}</td>
                    <td className="table-cell">{o.temperature}</td>
                    <td className={`table-cell ${hrClass(o.heartRate)}`}>{o.heartRate} bpm</td>
                    <td className="table-cell">{o.weight}</td>
                    <td className="table-cell">{o.height}</td>
                    <td className="table-cell">{o.recordedDate}</td>
                    <td className="table-cell">
                      <div className="flex gap-2">
                        <button onClick={() => openEdit(o)} className="btn-edit">Edit</button>
                        <button onClick={() => setConfirm({ open: true, id: o.observationId })} className="btn-danger">Delete</button>
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
        title={modal.mode === 'add' ? 'Record Vitals' : 'Edit Observation'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="sm:col-span-2">
              <label className="form-label">Patient *</label>
              <select className={errors.patientId ? 'form-input-error' : 'form-input'} value={form.patientId}
                onChange={e => setForm(f => ({ ...f, patientId: e.target.value }))}>
                <option value="">Select patient</option>
                {patients.map(p => <option key={p.patientId} value={p.patientId}>{p.fullName}</option>)}
              </select>
              {errors.patientId && <p className="text-xs text-red-500 mt-1">{errors.patientId}</p>}
            </div>
            <div>
              <label className="form-label">Blood Pressure *</label>
              <input className={errors.bloodPressure ? 'form-input-error' : 'form-input'} value={form.bloodPressure}
                onChange={e => setForm(f => ({ ...f, bloodPressure: e.target.value }))} placeholder="e.g. 120/80" />
              {errors.bloodPressure && <p className="text-xs text-red-500 mt-1">{errors.bloodPressure}</p>}
            </div>
            <div>
              <label className="form-label">Temperature (°C) *</label>
              <input type="number" step="0.1" min="30" max="45"
                className={errors.temperature ? 'form-input-error' : 'form-input'} value={form.temperature}
                onChange={e => setForm(f => ({ ...f, temperature: e.target.value }))} placeholder="e.g. 37.2" />
              {errors.temperature && <p className="text-xs text-red-500 mt-1">{errors.temperature}</p>}
            </div>
            <div>
              <label className="form-label">Heart Rate (bpm) *</label>
              <input type="number" min="30" max="300"
                className={errors.heartRate ? 'form-input-error' : 'form-input'} value={form.heartRate}
                onChange={e => setForm(f => ({ ...f, heartRate: e.target.value }))} placeholder="e.g. 72" />
              {errors.heartRate && <p className="text-xs text-red-500 mt-1">{errors.heartRate}</p>}
            </div>
            <div>
              <label className="form-label">Weight (kg) *</label>
              <input type="number" step="0.1" min="1" max="500"
                className={errors.weight ? 'form-input-error' : 'form-input'} value={form.weight}
                onChange={e => setForm(f => ({ ...f, weight: e.target.value }))} placeholder="e.g. 70.5" />
              {errors.weight && <p className="text-xs text-red-500 mt-1">{errors.weight}</p>}
            </div>
            <div>
              <label className="form-label">Height (cm) *</label>
              <input type="number" step="0.1" min="30" max="300"
                className={errors.height ? 'form-input-error' : 'form-input'} value={form.height}
                onChange={e => setForm(f => ({ ...f, height: e.target.value }))} placeholder="e.g. 175.0" />
              {errors.height && <p className="text-xs text-red-500 mt-1">{errors.height}</p>}
            </div>
            <div>
              <label className="form-label">Recorded Date *</label>
              <input type="date" className={errors.recordedDate ? 'form-input-error' : 'form-input'}
                value={form.recordedDate} max={new Date().toISOString().split('T')[0]}
                onChange={e => setForm(f => ({ ...f, recordedDate: e.target.value }))} />
              {errors.recordedDate && <p className="text-xs text-red-500 mt-1">{errors.recordedDate}</p>}
            </div>
          </div>
          <div className="flex gap-3 justify-end pt-2 border-t border-slate-100">
            <button type="button" onClick={() => setModal({ open: false })} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={saving} className="btn-primary">
              {saving && <Spinner size="sm" />}
              {modal.mode === 'add' ? 'Record Vitals' : 'Save Changes'}
            </button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog open={confirm.open} onConfirm={handleDelete} onCancel={() => setConfirm({ open: false })} />
    </div>
  )
}

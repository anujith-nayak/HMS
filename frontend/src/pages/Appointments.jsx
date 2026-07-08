import { useEffect, useState, useCallback } from 'react'
import toast from 'react-hot-toast'
import { getAppointments, createAppointment, updateAppointment, deleteAppointment } from '../api/appointments'
import { getPatients } from '../api/patients'
import { getDoctors } from '../api/doctors'
import Modal from '../components/Modal'
import ConfirmDialog from '../components/ConfirmDialog'
import Spinner from '../components/Spinner'
import SearchBar from '../components/SearchBar'
import EmptyState from '../components/EmptyState'

const EMPTY = { patientId: '', doctorId: '', appointmentDate: '', appointmentTime: '', status: 'SCHEDULED' }

const STATUS_STYLES = {
  SCHEDULED:  'bg-blue-100 text-blue-700',
  COMPLETED:  'bg-green-100 text-green-700',
  CANCELLED:  'bg-red-100 text-red-700',
  PENDING:    'bg-yellow-100 text-yellow-700',
  NO_SHOW:    'bg-slate-100 text-slate-600',
}

export default function Appointments() {
  const [appointments, setAppointments] = useState([])
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
    getAppointments(q)
      .then(r => setAppointments(r.data))
      .catch(() => toast.error('Failed to load appointments'))
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
  const openEdit = (a) => {
    setForm({
      patientId: a.patientId,
      doctorId: a.doctorId,
      appointmentDate: a.appointmentDate,
      appointmentTime: a.appointmentTime,
      status: a.status,
    })
    setErrors({})
    setModal({ open: true, mode: 'edit', data: a })
  }

  const validate = () => {
    const e = {}
    if (!form.patientId) e.patientId = 'Patient is required'
    if (!form.doctorId) e.doctorId = 'Doctor is required'
    if (!form.appointmentDate) e.appointmentDate = 'Date is required'
    if (!form.appointmentTime) e.appointmentTime = 'Time is required'
    if (!form.status) e.status = 'Status is required'
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
        await createAppointment(payload); toast.success('Appointment booked')
      } else {
        await updateAppointment(modal.data.appointmentId, payload); toast.success('Appointment updated')
      }
      setModal({ open: false }); load(search)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Operation failed')
    } finally { setSaving(false) }
  }

  const handleDelete = async () => {
    try { await deleteAppointment(confirm.id); toast.success('Appointment deleted'); load(search) }
    catch { toast.error('Delete failed') }
    finally { setConfirm({ open: false }) }
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <SearchBar value={search} onChange={setSearch} placeholder="Search by patient, doctor, status…" />
        <button onClick={openAdd} className="btn-primary">
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          Book Appointment
        </button>
      </div>

      <div className="card p-0 overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-16"><Spinner size="lg" /></div>
        ) : appointments.length === 0 ? (
          <EmptyState icon="📅" title="No appointments found" subtitle="Book an appointment to get started."
            action={<button onClick={openAdd} className="btn-primary">Book Appointment</button>} />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-slate-50 border-b border-slate-200">
                <tr>
                  <th className="table-header">#</th>
                  <th className="table-header">Patient</th>
                  <th className="table-header">Doctor</th>
                  <th className="table-header">Date</th>
                  <th className="table-header">Time</th>
                  <th className="table-header">Status</th>
                  <th className="table-header">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {appointments.map((a, i) => (
                  <tr key={a.appointmentId} className="hover:bg-slate-50 transition-colors">
                    <td className="table-cell text-slate-400 text-xs">{i + 1}</td>
                    <td className="table-cell font-medium text-slate-800">{a.patientName}</td>
                    <td className="table-cell text-slate-600">Dr. {a.doctorName}</td>
                    <td className="table-cell">{a.appointmentDate}</td>
                    <td className="table-cell">{a.appointmentTime}</td>
                    <td className="table-cell">
                      <span className={`badge ${STATUS_STYLES[a.status] || 'bg-slate-100 text-slate-600'}`}>
                        {a.status}
                      </span>
                    </td>
                    <td className="table-cell">
                      <div className="flex gap-2">
                        <button onClick={() => openEdit(a)} className="btn-edit">Edit</button>
                        <button onClick={() => setConfirm({ open: true, id: a.appointmentId })} className="btn-danger">Delete</button>
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
        title={modal.mode === 'add' ? 'Book Appointment' : 'Edit Appointment'}>
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
                {doctors.map(d => <option key={d.doctorId} value={d.doctorId}>Dr. {d.fullName} — {d.specialization}</option>)}
              </select>
              {errors.doctorId && <p className="text-xs text-red-500 mt-1">{errors.doctorId}</p>}
            </div>
            <div>
              <label className="form-label">Date *</label>
              <input type="date" className={errors.appointmentDate ? 'form-input-error' : 'form-input'}
                value={form.appointmentDate}
                onChange={e => setForm(f => ({ ...f, appointmentDate: e.target.value }))} />
              {errors.appointmentDate && <p className="text-xs text-red-500 mt-1">{errors.appointmentDate}</p>}
            </div>
            <div>
              <label className="form-label">Time *</label>
              <input type="time" className={errors.appointmentTime ? 'form-input-error' : 'form-input'}
                value={form.appointmentTime}
                onChange={e => setForm(f => ({ ...f, appointmentTime: e.target.value }))} />
              {errors.appointmentTime && <p className="text-xs text-red-500 mt-1">{errors.appointmentTime}</p>}
            </div>
            <div className="sm:col-span-2">
              <label className="form-label">Status *</label>
              <select className={errors.status ? 'form-input-error' : 'form-input'} value={form.status}
                onChange={e => setForm(f => ({ ...f, status: e.target.value }))}>
                {['SCHEDULED', 'PENDING', 'COMPLETED', 'CANCELLED', 'NO_SHOW'].map(s =>
                  <option key={s}>{s}</option>
                )}
              </select>
              {errors.status && <p className="text-xs text-red-500 mt-1">{errors.status}</p>}
            </div>
          </div>
          <div className="flex gap-3 justify-end pt-2 border-t border-slate-100">
            <button type="button" onClick={() => setModal({ open: false })} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={saving} className="btn-primary">
              {saving && <Spinner size="sm" />}
              {modal.mode === 'add' ? 'Book Appointment' : 'Save Changes'}
            </button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog open={confirm.open} onConfirm={handleDelete} onCancel={() => setConfirm({ open: false })} />
    </div>
  )
}

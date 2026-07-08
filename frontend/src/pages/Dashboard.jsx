import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getDashboardStats } from '../api/dashboard'
import Spinner from '../components/Spinner'

const statCards = [
  { key: 'totalPatients',      label: 'Total Patients',      icon: '🧑‍⚕️', color: 'bg-blue-500',   light: 'bg-blue-50',   text: 'text-blue-600',  to: '/patients' },
  { key: 'totalDoctors',       label: 'Total Doctors',       icon: '👨‍⚕️', color: 'bg-teal-500',   light: 'bg-teal-50',   text: 'text-teal-600',  to: '/doctors' },
  { key: 'totalAppointments',  label: 'Total Appointments',  icon: '📅', color: 'bg-purple-500', light: 'bg-purple-50', text: 'text-purple-600',to: '/appointments' },
  { key: 'totalPrescriptions', label: 'Total Prescriptions', icon: '💊', color: 'bg-rose-500',   light: 'bg-rose-50',   text: 'text-rose-600',  to: '/prescriptions' },
  { key: 'totalObservations',  label: 'Total Observations',  icon: '🩺', color: 'bg-amber-500',  light: 'bg-amber-50',  text: 'text-amber-600', to: '/observations' },
]

export default function Dashboard() {
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getDashboardStats()
      .then(r => setStats(r.data))
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="space-y-6">
      {/* Hero */}
      <div className="bg-gradient-to-r from-primary-700 to-primary-900 rounded-2xl p-6 text-white">
        <p className="text-primary-200 text-sm mb-1">Welcome back 👋</p>
        <h2 className="text-2xl font-bold mb-1">ABDM Hospital Management System</h2>
        <p className="text-primary-300 text-sm">Manage patients, doctors, appointments, prescriptions, and observations — all in one place.</p>
      </div>

      {/* Stats */}
      {loading ? (
        <div className="flex justify-center py-12"><Spinner size="lg" /></div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-4">
          {statCards.map(({ key, label, icon, light, text, to }) => (
            <Link key={key} to={to} className={`card ${light} border-0 hover:shadow-md transition-shadow group`}>
              <div className="flex items-center justify-between mb-3">
                <span className="text-2xl">{icon}</span>
                <svg className={`w-4 h-4 ${text} opacity-0 group-hover:opacity-100 transition-opacity`}
                  fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </div>
              <p className={`text-3xl font-bold ${text} mb-1`}>
                {stats ? stats[key] : '—'}
              </p>
              <p className="text-xs font-medium text-slate-600">{label}</p>
            </Link>
          ))}
        </div>
      )}

      {/* Quick links */}
      <div className="card">
        <h3 className="text-sm font-semibold text-slate-700 mb-4">Quick Actions</h3>
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-3">
          {[
            { to: '/patients',      label: 'Add Patient',      icon: '➕' },
            { to: '/doctors',       label: 'Add Doctor',       icon: '➕' },
            { to: '/appointments',  label: 'Book Appointment', icon: '📋' },
            { to: '/prescriptions', label: 'New Prescription', icon: '💊' },
            { to: '/observations',  label: 'Record Vitals',    icon: '🩺' },
          ].map(({ to, label, icon }) => (
            <Link
              key={to}
              to={to}
              className="flex flex-col items-center gap-2 p-3 rounded-xl bg-slate-50 hover:bg-primary-50 border border-slate-200 hover:border-primary-200 transition-colors text-center"
            >
              <span className="text-xl">{icon}</span>
              <span className="text-xs font-medium text-slate-600">{label}</span>
            </Link>
          ))}
        </div>
      </div>
    </div>
  )
}

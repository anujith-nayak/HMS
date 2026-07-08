import { NavLink } from 'react-router-dom'

const navItems = [
  { to: '/dashboard',    label: 'Dashboard',     icon: '🏠' },
  { to: '/patients',     label: 'Patients',       icon: '🧑‍⚕️' },
  { to: '/doctors',      label: 'Doctors',        icon: '👨‍⚕️' },
  { to: '/appointments', label: 'Appointments',   icon: '📅' },
  { to: '/prescriptions',label: 'Prescriptions',  icon: '💊' },
  { to: '/observations', label: 'Observations',   icon: '🩺' },
]

export default function Sidebar({ open, onClose }) {
  return (
    <>
      {/* Mobile overlay */}
      {open && (
        <div
          className="fixed inset-0 bg-black/40 z-20 lg:hidden"
          onClick={onClose}
        />
      )}

      <aside className={`
        fixed top-0 left-0 h-full z-30 flex flex-col
        w-64 bg-gradient-to-b from-primary-800 to-primary-900 text-white shadow-2xl
        transform transition-transform duration-300
        ${open ? 'translate-x-0' : '-translate-x-full'}
        lg:translate-x-0 lg:static lg:z-auto
      `}>
        {/* Logo */}
        <div className="flex items-center gap-3 px-6 py-5 border-b border-primary-700">
          <div className="w-9 h-9 bg-white/20 rounded-lg flex items-center justify-center text-xl">🏥</div>
          <div>
            <p className="font-bold text-sm leading-tight">ABDM-HMS</p>
            <p className="text-primary-300 text-xs">Hospital Management</p>
          </div>
        </div>

        {/* Nav */}
        <nav className="flex-1 px-3 py-4 overflow-y-auto">
          <p className="px-3 mb-2 text-xs font-semibold text-primary-400 uppercase tracking-wider">Navigation</p>
          {navItems.map(({ to, label, icon }) => (
            <NavLink
              key={to}
              to={to}
              onClick={onClose}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg mb-0.5 text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-white/20 text-white'
                    : 'text-primary-200 hover:bg-white/10 hover:text-white'
                }`
              }
            >
              <span className="text-base">{icon}</span>
              {label}
            </NavLink>
          ))}
        </nav>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-primary-700">
          <p className="text-xs text-primary-400">FHIR-Ready Platform</p>
          <p className="text-xs text-primary-500">v1.0.0</p>
        </div>
      </aside>
    </>
  )
}

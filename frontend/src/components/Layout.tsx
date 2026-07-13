import React from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { LayoutDashboard, Layers, Upload, Search, LogOut, Zap } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

const navItems = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/batches', icon: Layers, label: 'Batch CVs' },
  { to: '/upload', icon: Upload, label: 'Upload CV' },
  { to: '/search', icon: Search, label: 'Tìm kiếm' },
]

const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { pathname } = useLocation()
  const { logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="flex min-h-screen">
      {/* Sidebar */}
      <aside className="w-64 flex-shrink-0 flex flex-col"
        style={{ background: 'var(--color-surface)', borderRight: '1px solid var(--color-border)' }}>

        {/* Logo */}
        <div className="flex items-center gap-3 px-6 py-5" style={{ borderBottom: '1px solid var(--color-border)' }}>
          <div className="w-9 h-9 rounded-xl flex items-center justify-center animate-pulse-glow"
            style={{ background: 'linear-gradient(135deg, #6366f1, #a78bfa)' }}>
            <Zap size={18} className="text-white" />
          </div>
          <div>
            <p className="font-bold gradient-text text-sm leading-tight">ResumeSearch</p>
            <p className="text-xs" style={{ color: 'var(--color-text-muted)' }}>AI Engine</p>
          </div>
        </div>

        {/* Nav */}
        <nav className="flex-1 px-3 py-4 space-y-1">
          {navItems.map(({ to, icon: Icon, label }) => {
            const active = pathname.startsWith(to)
            return (
              <Link key={to} to={to}
                className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200"
                style={{
                  background: active ? 'rgba(99,102,241,0.15)' : 'transparent',
                  color: active ? '#818cf8' : 'var(--color-text-muted)',
                  borderLeft: active ? '2px solid #6366f1' : '2px solid transparent',
                }}>
                <Icon size={18} />
                {label}
              </Link>
            )
          })}
        </nav>

        {/* Logout */}
        <div className="px-3 pb-4" style={{ borderTop: '1px solid var(--color-border)', paddingTop: '1rem' }}>
          <button onClick={handleLogout}
            className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium w-full transition-all duration-200"
            style={{ color: 'var(--color-text-muted)' }}
            onMouseOver={e => (e.currentTarget.style.color = '#f87171')}
            onMouseOut={e => (e.currentTarget.style.color = 'var(--color-text-muted)')}>
            <LogOut size={18} />
            Đăng xuất
          </button>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 overflow-auto p-8 animate-fadeInUp">
        {children}
      </main>
    </div>
  )
}

export default Layout

import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Eye, EyeOff, LogIn, UserPlus, Zap } from 'lucide-react'
import toast from 'react-hot-toast'
import { authService } from '../services'
import { useAuth } from '../context/AuthContext'

type Mode = 'login' | 'register'

const AuthPage: React.FC = () => {
  const [mode, setMode] = useState<Mode>('login')
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPass, setShowPass] = useState(false)
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    try {
      if (mode === 'login') {
        const res = await authService.login(username, password)
        const token = res.data?.data?.token
        if (token) {
          login(token)
          toast.success('Đăng nhập thành công!')
          navigate('/dashboard')
        }
      } else {
        await authService.register(username, password, email)
        toast.success('Đăng ký thành công! Hãy đăng nhập.')
        setMode('login')
      }
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Thao tác thất bại')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center relative overflow-hidden p-4">
      {/* Background blobs */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -left-40 w-96 h-96 rounded-full opacity-20"
          style={{ background: 'radial-gradient(circle, #6366f1, transparent)' }} />
        <div className="absolute -bottom-40 -right-40 w-96 h-96 rounded-full opacity-20"
          style={{ background: 'radial-gradient(circle, #a78bfa, transparent)' }} />
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-72 h-72 rounded-full opacity-10"
          style={{ background: 'radial-gradient(circle, #38bdf8, transparent)' }} />
      </div>

      <div className="w-full max-w-md animate-fadeInUp relative z-10">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl mb-4 animate-pulse-glow"
            style={{ background: 'linear-gradient(135deg, #6366f1, #a78bfa)' }}>
            <Zap size={28} className="text-white" />
          </div>
          <h1 className="text-3xl font-bold gradient-text">ResumeSearch</h1>
          <p className="text-sm mt-1" style={{ color: 'var(--color-text-muted)' }}>
            AI-Powered CV Screening Engine
          </p>
        </div>

        {/* Card */}
        <div className="glass p-8">
          {/* Tab switcher */}
          <div className="flex rounded-lg p-1 mb-6" style={{ background: 'rgba(255,255,255,0.05)' }}>
            {(['login', 'register'] as Mode[]).map((m) => (
              <button key={m} onClick={() => setMode(m)}
                className="flex-1 py-2 rounded-md text-sm font-medium transition-all duration-200"
                style={{
                  background: mode === m ? 'linear-gradient(135deg, #6366f1, #4f46e5)' : 'transparent',
                  color: mode === m ? 'white' : 'var(--color-text-muted)',
                }}>
                {m === 'login' ? 'Đăng nhập' : 'Đăng ký'}
              </button>
            ))}
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-1.5" style={{ color: 'var(--color-text-muted)' }}>
                Tên đăng nhập
              </label>
              <input className="input-base" type="text" placeholder="username"
                value={username} onChange={e => setUsername(e.target.value)} required />
            </div>

            {mode === 'register' && (
              <div>
                <label className="block text-sm font-medium mb-1.5" style={{ color: 'var(--color-text-muted)' }}>
                  Email
                </label>
                <input className="input-base" type="email" placeholder="email@example.com"
                  value={email} onChange={e => setEmail(e.target.value)} required />
              </div>
            )}

            <div>
              <label className="block text-sm font-medium mb-1.5" style={{ color: 'var(--color-text-muted)' }}>
                Mật khẩu
              </label>
              <div className="relative">
                <input className="input-base pr-10" type={showPass ? 'text' : 'password'}
                  placeholder="••••••••" value={password} onChange={e => setPassword(e.target.value)} required />
                <button type="button" onClick={() => setShowPass(!showPass)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 transition-colors"
                  style={{ color: 'var(--color-text-muted)' }}>
                  {showPass ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>

            <button type="submit" className="btn-primary w-full justify-center mt-6" disabled={loading}>
              {loading ? (
                <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : mode === 'login' ? (
                <><LogIn size={16} /> Đăng nhập</>
              ) : (
                <><UserPlus size={16} /> Tạo tài khoản</>
              )}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}

export default AuthPage

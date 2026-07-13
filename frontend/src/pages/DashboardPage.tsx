import React from 'react'
import { Layers, Upload, Search, Activity, ArrowRight } from 'lucide-react'
import { Link } from 'react-router-dom'

const stats = [
  { label: 'Tổng Batch', value: '—', icon: Layers, color: '#6366f1' },
  { label: 'CV đã xử lý', value: '—', icon: Upload, color: '#a78bfa' },
  { label: 'Tìm kiếm hôm nay', value: '—', icon: Search, color: '#38bdf8' },
  { label: 'ETL Queue', value: 'Running', icon: Activity, color: '#4ade80' },
]

const DashboardPage: React.FC = () => {
  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-3xl font-bold gradient-text">Dashboard</h1>
          <p className="mt-1 text-sm" style={{ color: 'var(--color-text-muted)' }}>
            Tổng quan hệ thống CV ETL & Hybrid Search
          </p>
        </div>
        <div className="flex gap-3">
          <Link to="/upload" className="btn-primary">
            <Upload size={16} /> Upload CV
          </Link>
        </div>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map(({ label, value, icon: Icon, color }) => (
          <div key={label} className="glass p-5 flex items-center gap-4 hover:glow transition-all duration-300">
            <div className="w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0"
              style={{ background: `${color}20`, border: `1px solid ${color}40` }}>
              <Icon size={22} style={{ color }} />
            </div>
            <div>
              <p className="text-xl font-bold">{value}</p>
              <p className="text-xs" style={{ color: 'var(--color-text-muted)' }}>{label}</p>
            </div>
          </div>
        ))}
      </div>

      {/* Architecture overview */}
      <div className="glass p-6">
        <h2 className="text-lg font-semibold mb-4 flex items-center gap-2">
          <Activity size={18} style={{ color: 'var(--color-primary)' }} />
          Luồng xử lý CV
        </h2>
        <div className="flex flex-wrap items-center gap-3">
          {[
            { label: 'Upload CV', color: '#6366f1' },
            { label: 'MinIO Storage', color: '#a78bfa' },
            { label: 'Kafka Queue', color: '#38bdf8' },
            { label: 'OCR / PDF Parse', color: '#fbbf24' },
            { label: 'LLM Extraction', color: '#4ade80' },
            { label: 'Structured JSON', color: '#f87171' },
          ].map(({ label, color }, i, arr) => (
            <React.Fragment key={label}>
              <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-medium"
                style={{ background: `${color}15`, border: `1px solid ${color}40`, color }}>
                <span className="w-1.5 h-1.5 rounded-full" style={{ background: color }} />
                {label}
              </div>
              {i < arr.length - 1 && <ArrowRight size={14} style={{ color: 'var(--color-text-muted)' }} />}
            </React.Fragment>
          ))}
        </div>
      </div>

      {/* Quick actions */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {[
          { to: '/batches', icon: Layers, title: 'Quản lý Batch', desc: 'Tạo, sửa, xóa batch CV — định nghĩa kỹ năng yêu cầu', color: '#6366f1' },
          { to: '/upload', icon: Upload, title: 'Upload CV', desc: 'Upload file PDF vào batch để ETL tự động xử lý', color: '#a78bfa' },
          { to: '/search', icon: Search, title: 'Tìm kiếm Ứng viên', desc: 'Hybrid search: Keyword + Semantic theo batch', color: '#38bdf8' },
        ].map(({ to, icon: Icon, title, desc, color }) => (
          <Link key={to} to={to}
            className="glass p-5 group hover:glow transition-all duration-300 block"
            style={{ textDecoration: 'none' }}>
            <div className="flex items-start gap-4">
              <div className="w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0 transition-transform group-hover:scale-110"
                style={{ background: `${color}20` }}>
                <Icon size={20} style={{ color }} />
              </div>
              <div className="flex-1 min-w-0">
                <p className="font-semibold text-sm">{title}</p>
                <p className="text-xs mt-0.5 leading-relaxed" style={{ color: 'var(--color-text-muted)' }}>{desc}</p>
              </div>
              <ArrowRight size={16} className="flex-shrink-0 opacity-0 group-hover:opacity-100 transition-opacity"
                style={{ color }} />
            </div>
          </Link>
        ))}
      </div>
    </div>
  )
}

export default DashboardPage

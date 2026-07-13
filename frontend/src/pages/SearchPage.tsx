import React from 'react'
import { Construction } from 'lucide-react'

// Placeholder — sẽ kết nối search-service khi backend hoàn thiện
const SearchPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold gradient-text">Tìm kiếm Ứng viên</h1>
        <p className="mt-1 text-sm" style={{ color: 'var(--color-text-muted)' }}>
          Hybrid Search: Keyword + Semantic (Elasticsearch + Weaviate)
        </p>
      </div>

      <div className="glass p-10 flex flex-col items-center justify-center text-center gap-4">
        <div className="w-16 h-16 rounded-2xl flex items-center justify-center"
          style={{ background: 'rgba(251,191,36,0.1)', border: '1px solid rgba(251,191,36,0.3)' }}>
          <Construction size={32} style={{ color: '#fbbf24' }} />
        </div>
        <div>
          <p className="font-bold text-lg">Đang phát triển</p>
          <p className="text-sm mt-1 max-w-md" style={{ color: 'var(--color-text-muted)' }}>
            Tính năng Hybrid Search sẽ được tích hợp khi <strong>search-service</strong> (port 8083)
            và <strong>candidate-service</strong> (port 8084) hoàn thiện.
          </p>
        </div>
        <div className="flex flex-wrap gap-2 justify-center mt-2">
          {['Keyword Search', 'Semantic Search', 'Weaviate Vector DB', 'Ranking Engine'].map(t => (
            <span key={t} className="badge badge-warning">{t}</span>
          ))}
        </div>
      </div>
    </div>
  )
}

export default SearchPage

import React, { useState, useCallback } from 'react'
import { Upload, FileText, CheckCircle2, XCircle, Loader2, X, ChevronDown } from 'lucide-react'
import toast from 'react-hot-toast'
import { resumeService, batchService } from '../services'

interface UploadResult {
  file: string
  status: 'success' | 'error'
  message: string
  resumeId?: number
}

const UploadPage: React.FC = () => {
  const [files, setFiles] = useState<File[]>([])
  const [dragging, setDragging] = useState(false)
  const [batchId, setBatchId] = useState('')
  const [batches, setBatches] = useState<{ id: string; batchName: string }[]>([])
  const [results, setResults] = useState<UploadResult[]>([])
  const [uploading, setUploading] = useState(false)
  const [progress, setProgress] = useState(0)

  const loadBatches = async () => {
    if (batches.length > 0) return
    try {
      const res = await batchService.getAll()
      setBatches(res.data?.data || [])
    } catch {}
  }

  const onDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault(); setDragging(false)
    const dropped = Array.from(e.dataTransfer.files).filter(f => f.name.endsWith('.pdf'))
    if (dropped.length === 0) return toast.error('Chỉ chấp nhận file PDF')
    setFiles(prev => [...prev, ...dropped])
  }, [])

  const onFileInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selected = Array.from(e.target.files || []).filter(f => f.name.endsWith('.pdf'))
    setFiles(prev => [...prev, ...selected])
    e.target.value = ''
  }

  const removeFile = (i: number) => setFiles(prev => prev.filter((_, idx) => idx !== i))

  const handleUpload = async () => {
    if (files.length === 0) return toast.error('Chưa chọn file nào')
    setUploading(true); setResults([]); setProgress(0)
    const out: UploadResult[] = []
    for (let i = 0; i < files.length; i++) {
      const f = files[i]
      try {
        const res = await resumeService.upload(f, batchId || undefined)
        out.push({ file: f.name, status: 'success', message: 'Upload thành công', resumeId: res.data?.data?.resumeId })
      } catch (err: any) {
        out.push({ file: f.name, status: 'error', message: err.response?.data?.message || 'Upload thất bại' })
      }
      setProgress(Math.round(((i + 1) / files.length) * 100))
    }
    setResults(out)
    setFiles([])
    setUploading(false)
    const ok = out.filter(r => r.status === 'success').length
    toast.success(`${ok}/${out.length} file upload thành công`)
  }

  return (
    <div className="space-y-6 max-w-2xl">
      <div>
        <h1 className="text-3xl font-bold gradient-text">Upload CV</h1>
        <p className="mt-1 text-sm" style={{ color: 'var(--color-text-muted)' }}>
          Upload file PDF — hệ thống sẽ tự động OCR và xử lý qua Kafka ETL
        </p>
      </div>

      {/* Batch selector */}
      <div className="glass p-5 space-y-3">
        <label className="block text-sm font-medium" style={{ color: 'var(--color-text-muted)' }}>
          Chọn Batch (tuỳ chọn)
        </label>
        <div className="relative">
          <select className="input-base pr-8 appearance-none cursor-pointer"
            value={batchId} onChange={e => setBatchId(e.target.value)} onFocus={loadBatches}>
            <option value="">-- Không thuộc batch nào --</option>
            {batches.map(b => <option key={b.id} value={b.id}>{b.batchName}</option>)}
          </select>
          <ChevronDown size={14} className="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" style={{ color: 'var(--color-text-muted)' }} />
        </div>
      </div>

      {/* Drop zone */}
      <div
        onDragOver={e => { e.preventDefault(); setDragging(true) }}
        onDragLeave={() => setDragging(false)}
        onDrop={onDrop}
        className="glass rounded-2xl border-2 border-dashed p-10 flex flex-col items-center gap-3 cursor-pointer transition-all duration-300"
        style={{ borderColor: dragging ? 'var(--color-primary)' : 'var(--color-border)', background: dragging ? 'rgba(99,102,241,0.05)' : undefined }}
        onClick={() => document.getElementById('file-input')?.click()}>
        <div className="w-14 h-14 rounded-2xl flex items-center justify-center"
          style={{ background: 'rgba(99,102,241,0.15)' }}>
          <Upload size={26} style={{ color: 'var(--color-primary)' }} />
        </div>
        <div className="text-center">
          <p className="font-semibold">Kéo thả file PDF vào đây</p>
          <p className="text-sm mt-1" style={{ color: 'var(--color-text-muted)' }}>hoặc click để chọn file — chỉ chấp nhận .pdf</p>
        </div>
        <input id="file-input" type="file" accept=".pdf" multiple className="hidden" onChange={onFileInput} />
      </div>

      {/* File list */}
      {files.length > 0 && (
        <div className="glass p-4 space-y-2">
          <p className="text-sm font-medium" style={{ color: 'var(--color-text-muted)' }}>{files.length} file đã chọn</p>
          {files.map((f, i) => (
            <div key={i} className="flex items-center gap-3 p-2.5 rounded-lg" style={{ background: 'rgba(255,255,255,0.04)' }}>
              <FileText size={16} style={{ color: 'var(--color-primary)' }} />
              <span className="flex-1 text-sm truncate">{f.name}</span>
              <span className="text-xs" style={{ color: 'var(--color-text-muted)' }}>{(f.size / 1024).toFixed(0)} KB</span>
              <button onClick={() => removeFile(i)} className="p-1 hover:bg-white/10 rounded transition-colors"><X size={14} /></button>
            </div>
          ))}
        </div>
      )}

      {/* Progress */}
      {uploading && (
        <div className="glass p-4 space-y-2">
          <div className="flex items-center justify-between text-sm">
            <span className="flex items-center gap-2"><Loader2 size={14} className="animate-spin" style={{ color: 'var(--color-primary)' }} />Đang upload...</span>
            <span style={{ color: 'var(--color-primary)' }}>{progress}%</span>
          </div>
          <div className="h-1.5 rounded-full overflow-hidden" style={{ background: 'rgba(255,255,255,0.1)' }}>
            <div className="h-full rounded-full transition-all duration-300" style={{ width: `${progress}%`, background: 'linear-gradient(90deg, #6366f1, #a78bfa)' }} />
          </div>
        </div>
      )}

      {/* Upload button */}
      <button onClick={handleUpload} disabled={uploading || files.length === 0} className="btn-primary w-full justify-center">
        {uploading ? <><Loader2 size={16} className="animate-spin" />Đang xử lý...</> : <><Upload size={16} />Upload {files.length > 0 ? `${files.length} file` : 'CV'}</>}
      </button>

      {/* Results */}
      {results.length > 0 && (
        <div className="glass p-4 space-y-2">
          <p className="text-sm font-semibold mb-3">Kết quả upload</p>
          {results.map((r, i) => (
            <div key={i} className="flex items-center gap-3 p-2.5 rounded-lg" style={{ background: 'rgba(255,255,255,0.04)' }}>
              {r.status === 'success'
                ? <CheckCircle2 size={16} style={{ color: '#4ade80' }} />
                : <XCircle size={16} style={{ color: '#f87171' }} />}
              <span className="flex-1 text-sm truncate">{r.file}</span>
              <span className="text-xs" style={{ color: r.status === 'success' ? '#4ade80' : '#f87171' }}>{r.message}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default UploadPage

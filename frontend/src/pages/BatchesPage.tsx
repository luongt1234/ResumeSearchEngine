import React, { useState, useEffect } from 'react'
import { Plus, Pencil, Trash2, Tag, ChevronRight, X, Check, Loader2 } from 'lucide-react'
import toast from 'react-hot-toast'
import { batchService, skillService } from '../services'

interface Batch {
  id: string
  batchName: string
  targetPosition: string
  minYoe: number
  description: string
  createdAt: string
}

interface Skill {
  id: string
  skillName: string
  weight: number
  isMandatory: boolean
}

const emptyBatch = { batchName: '', targetPosition: '', minYoe: 0, description: '' }
const emptySkill = { skillName: '', weight: 1, isMandatory: false }

const BatchesPage: React.FC = () => {
  const [batches, setBatches] = useState<Batch[]>([])
  const [loading, setLoading] = useState(true)
  const [selectedBatch, setSelectedBatch] = useState<Batch | null>(null)
  const [skills, setSkills] = useState<Skill[]>([])
  const [skillsLoading, setSkillsLoading] = useState(false)

  // Modals
  const [showBatchModal, setShowBatchModal] = useState(false)
  const [editBatch, setEditBatch] = useState<Batch | null>(null)
  const [batchForm, setBatchForm] = useState(emptyBatch)
  const [savingBatch, setSavingBatch] = useState(false)

  const [showSkillModal, setShowSkillModal] = useState(false)
  const [editSkill, setEditSkill] = useState<Skill | null>(null)
  const [skillForm, setSkillForm] = useState(emptySkill)
  const [savingSkill, setSavingSkill] = useState(false)

  useEffect(() => { fetchBatches() }, [])

  const fetchBatches = async () => {
    try {
      const res = await batchService.getAll()
      setBatches(res.data?.data || [])
    } catch { toast.error('Không tải được danh sách batch') }
    finally { setLoading(false) }
  }

  const fetchSkills = async (batchId: string) => {
    setSkillsLoading(true)
    try {
      const res = await skillService.getByBatch(batchId)
      setSkills(res.data?.data || [])
    } catch { toast.error('Không tải được kỹ năng') }
    finally { setSkillsLoading(false) }
  }

  const handleSelectBatch = (b: Batch) => {
    setSelectedBatch(b)
    fetchSkills(b.id)
  }

  // Batch CRUD
  const openCreateBatch = () => { setBatchForm(emptyBatch); setEditBatch(null); setShowBatchModal(true) }
  const openEditBatch = (b: Batch, e: React.MouseEvent) => {
    e.stopPropagation()
    setBatchForm({ batchName: b.batchName, targetPosition: b.targetPosition, minYoe: b.minYoe, description: b.description })
    setEditBatch(b); setShowBatchModal(true)
  }
  const saveBatch = async () => {
    setSavingBatch(true)
    try {
      if (editBatch) { await batchService.update(editBatch.id, batchForm); toast.success('Đã cập nhật batch') }
      else { await batchService.create(batchForm); toast.success('Đã tạo batch') }
      setShowBatchModal(false); fetchBatches()
    } catch { toast.error('Thao tác thất bại') }
    finally { setSavingBatch(false) }
  }
  const deleteBatch = async (id: string, e: React.MouseEvent) => {
    e.stopPropagation()
    if (!confirm('Xóa batch này?')) return
    try { await batchService.delete(id); toast.success('Đã xóa'); fetchBatches(); if (selectedBatch?.id === id) setSelectedBatch(null) }
    catch { toast.error('Xóa thất bại') }
  }

  // Skill CRUD
  const openAddSkill = () => { setSkillForm(emptySkill); setEditSkill(null); setShowSkillModal(true) }
  const openEditSkill = (s: Skill) => { setSkillForm({ skillName: s.skillName, weight: s.weight, isMandatory: s.isMandatory }); setEditSkill(s); setShowSkillModal(true) }
  const saveSkill = async () => {
    if (!selectedBatch) return
    setSavingSkill(true)
    try {
      if (editSkill) { await skillService.update(selectedBatch.id, editSkill.id, skillForm); toast.success('Đã cập nhật kỹ năng') }
      else { await skillService.add(selectedBatch.id, skillForm); toast.success('Đã thêm kỹ năng') }
      setShowSkillModal(false); fetchSkills(selectedBatch.id)
    } catch { toast.error('Thao tác thất bại') }
    finally { setSavingSkill(false) }
  }
  const removeSkill = async (skillId: string) => {
    if (!selectedBatch || !confirm('Xóa kỹ năng này?')) return
    try { await skillService.remove(selectedBatch.id, skillId); toast.success('Đã xóa kỹ năng'); fetchSkills(selectedBatch.id) }
    catch { toast.error('Xóa thất bại') }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold gradient-text">Batch CVs</h1>
          <p className="mt-1 text-sm" style={{ color: 'var(--color-text-muted)' }}>Quản lý batch tuyển dụng và kỹ năng yêu cầu</p>
        </div>
        <button onClick={openCreateBatch} className="btn-primary"><Plus size={16} />Tạo Batch</button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
        {/* Batch list */}
        <div className="lg:col-span-2 space-y-3">
          {loading ? (
            <div className="flex justify-center py-12"><Loader2 size={24} className="animate-spin" style={{ color: 'var(--color-primary)' }} /></div>
          ) : batches.length === 0 ? (
            <div className="glass p-8 text-center">
              <p style={{ color: 'var(--color-text-muted)' }}>Chưa có batch nào</p>
              <button onClick={openCreateBatch} className="btn-primary mt-4 mx-auto"><Plus size={14} />Tạo batch đầu tiên</button>
            </div>
          ) : batches.map(b => (
            <div key={b.id} onClick={() => handleSelectBatch(b)} className="glass p-4 cursor-pointer transition-all duration-200 hover:glow group"
              style={{ borderColor: selectedBatch?.id === b.id ? 'var(--color-primary)' : undefined }}>
              <div className="flex items-start justify-between">
                <div className="flex-1 min-w-0">
                  <p className="font-semibold text-sm truncate">{b.batchName}</p>
                  <p className="text-xs mt-0.5 truncate" style={{ color: 'var(--color-text-muted)' }}>{b.targetPosition}</p>
                  <p className="text-xs mt-1" style={{ color: 'var(--color-text-muted)' }}>Min YoE: {b.minYoe} năm</p>
                </div>
                <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity ml-2">
                  <button onClick={(e) => openEditBatch(b, e)} className="p-1.5 rounded-md transition-colors hover:bg-white/10"><Pencil size={13} /></button>
                  <button onClick={(e) => deleteBatch(b.id, e)} className="p-1.5 rounded-md transition-colors hover:bg-red-500/20" style={{ color: '#f87171' }}><Trash2 size={13} /></button>
                </div>
                <ChevronRight size={14} className="ml-1 flex-shrink-0" style={{ color: selectedBatch?.id === b.id ? 'var(--color-primary)' : 'var(--color-text-muted)' }} />
              </div>
            </div>
          ))}
        </div>

        {/* Skills panel */}
        <div className="lg:col-span-3">
          {!selectedBatch ? (
            <div className="glass p-10 flex flex-col items-center justify-center h-full text-center">
              <Tag size={32} style={{ color: 'var(--color-text-muted)' }} className="mb-3" />
              <p className="font-medium">Chọn một batch để xem kỹ năng</p>
              <p className="text-sm mt-1" style={{ color: 'var(--color-text-muted)' }}>Mỗi batch có thể có nhiều kỹ năng yêu cầu với trọng số khác nhau</p>
            </div>
          ) : (
            <div className="glass p-5 space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h2 className="font-bold">{selectedBatch.batchName}</h2>
                  <p className="text-xs" style={{ color: 'var(--color-text-muted)' }}>{selectedBatch.targetPosition}</p>
                </div>
                <button onClick={openAddSkill} className="btn-primary" style={{ padding: '0.4rem 1rem', fontSize: '0.8rem' }}><Plus size={14} />Thêm kỹ năng</button>
              </div>

              {skillsLoading ? (
                <div className="flex justify-center py-8"><Loader2 size={20} className="animate-spin" style={{ color: 'var(--color-primary)' }} /></div>
              ) : skills.length === 0 ? (
                <p className="text-sm text-center py-6" style={{ color: 'var(--color-text-muted)' }}>Chưa có kỹ năng nào</p>
              ) : (
                <div className="space-y-2">
                  {skills.map(s => (
                    <div key={s.id} className="flex items-center gap-3 p-3 rounded-lg" style={{ background: 'rgba(255,255,255,0.04)' }}>
                      <div className="flex-1">
                        <div className="flex items-center gap-2">
                          <span className="text-sm font-medium">{s.skillName}</span>
                          {s.isMandatory && <span className="badge badge-danger">Bắt buộc</span>}
                        </div>
                        <div className="flex items-center gap-1.5 mt-1">
                          <span className="text-xs" style={{ color: 'var(--color-text-muted)' }}>Trọng số:</span>
                          <div className="flex gap-0.5">
                            {Array.from({ length: 5 }).map((_, i) => (
                              <div key={i} className="w-3 h-1.5 rounded-full" style={{ background: i < s.weight ? 'var(--color-primary)' : 'rgba(255,255,255,0.1)' }} />
                            ))}
                          </div>
                          <span className="text-xs font-mono" style={{ color: 'var(--color-primary)' }}>{s.weight}/5</span>
                        </div>
                      </div>
                      <div className="flex gap-1">
                        <button onClick={() => openEditSkill(s)} className="p-1.5 rounded-md hover:bg-white/10 transition-colors"><Pencil size={13} /></button>
                        <button onClick={() => removeSkill(s.id)} className="p-1.5 rounded-md hover:bg-red-500/20 transition-colors" style={{ color: '#f87171' }}><Trash2 size={13} /></button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Batch Modal */}
      {showBatchModal && (
        <Modal title={editBatch ? 'Chỉnh sửa Batch' : 'Tạo Batch mới'} onClose={() => setShowBatchModal(false)}>
          <div className="space-y-4">
            <Field label="Tên batch"><input className="input-base" value={batchForm.batchName} onChange={e => setBatchForm(f => ({ ...f, batchName: e.target.value }))} placeholder="VD: Backend Dev Q3 2025" /></Field>
            <Field label="Vị trí tuyển"><input className="input-base" value={batchForm.targetPosition} onChange={e => setBatchForm(f => ({ ...f, targetPosition: e.target.value }))} placeholder="VD: Java Backend Developer" /></Field>
            <Field label="Số năm kinh nghiệm tối thiểu"><input className="input-base" type="number" min={0} value={batchForm.minYoe} onChange={e => setBatchForm(f => ({ ...f, minYoe: Number(e.target.value) }))} /></Field>
            <Field label="Mô tả"><textarea className="input-base" rows={3} value={batchForm.description} onChange={e => setBatchForm(f => ({ ...f, description: e.target.value }))} placeholder="Mô tả yêu cầu..." /></Field>
          </div>
          <div className="flex justify-end gap-3 mt-6">
            <button onClick={() => setShowBatchModal(false)} className="btn-ghost">Hủy</button>
            <button onClick={saveBatch} className="btn-primary" disabled={savingBatch}>
              {savingBatch ? <Loader2 size={14} className="animate-spin" /> : <Check size={14} />}
              {editBatch ? 'Cập nhật' : 'Tạo'}
            </button>
          </div>
        </Modal>
      )}

      {/* Skill Modal */}
      {showSkillModal && (
        <Modal title={editSkill ? 'Chỉnh sửa Kỹ năng' : 'Thêm Kỹ năng'} onClose={() => setShowSkillModal(false)}>
          <div className="space-y-4">
            <Field label="Tên kỹ năng"><input className="input-base" value={skillForm.skillName} onChange={e => setSkillForm(f => ({ ...f, skillName: e.target.value }))} placeholder="VD: Java, Spring Boot, Docker..." /></Field>
            <Field label={`Trọng số (${skillForm.weight}/5)`}>
              <input type="range" min={1} max={5} value={skillForm.weight} onChange={e => setSkillForm(f => ({ ...f, weight: Number(e.target.value) }))} className="w-full accent-indigo-500" />
            </Field>
            <div className="flex items-center gap-3">
              <input type="checkbox" id="mandatory" checked={skillForm.isMandatory} onChange={e => setSkillForm(f => ({ ...f, isMandatory: e.target.checked }))} className="w-4 h-4 accent-indigo-500" />
              <label htmlFor="mandatory" className="text-sm">Kỹ năng bắt buộc</label>
            </div>
          </div>
          <div className="flex justify-end gap-3 mt-6">
            <button onClick={() => setShowSkillModal(false)} className="btn-ghost">Hủy</button>
            <button onClick={saveSkill} className="btn-primary" disabled={savingSkill}>
              {savingSkill ? <Loader2 size={14} className="animate-spin" /> : <Check size={14} />}
              {editSkill ? 'Cập nhật' : 'Thêm'}
            </button>
          </div>
        </Modal>
      )}
    </div>
  )
}

const Modal: React.FC<{ title: string; onClose: () => void; children: React.ReactNode }> = ({ title, onClose, children }) => (
  <div className="fixed inset-0 z-50 flex items-center justify-center p-4" style={{ background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(4px)' }}>
    <div className="glass p-6 w-full max-w-md animate-fadeInUp">
      <div className="flex items-center justify-between mb-5">
        <h3 className="font-bold text-lg">{title}</h3>
        <button onClick={onClose} className="p-1.5 rounded-lg hover:bg-white/10 transition-colors"><X size={18} /></button>
      </div>
      {children}
    </div>
  </div>
)

const Field: React.FC<{ label: string; children: React.ReactNode }> = ({ label, children }) => (
  <div>
    <label className="block text-sm font-medium mb-1.5" style={{ color: 'var(--color-text-muted)' }}>{label}</label>
    {children}
  </div>
)

export default BatchesPage

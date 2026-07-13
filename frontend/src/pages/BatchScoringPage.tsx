import React, { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, RefreshCw, Loader2, Award, CheckCircle2, XCircle, ChevronDown, ChevronUp, Layers } from 'lucide-react'
import toast from 'react-hot-toast'
import { scoreService, batchService } from '../services'

interface ScoreBreakdown {
  semanticScore: number
  skillScore: number
  yoeScore: number
}

interface SkillMatch {
  requiredSkill: string
  matchedWith: string
  certainty: number
  isMandatory: boolean
}

interface CandidateScore {
  resumeId: string
  candidateName: string
  finalScore: number
  label: string
  hasMissingMandatory: boolean
  breakdown: ScoreBreakdown
  skillMatches: SkillMatch[]
  missingSkills: string[]
  missingMandatorySkills: string[]
}

interface ScoreResult {
  batchId: string
  batchName: string
  totalCandidates: number
  results: CandidateScore[]
}

const BatchScoringPage: React.FC = () => {
  const { batchId } = useParams<{ batchId: string }>()
  const navigate = useNavigate()

  const [loading, setLoading] = useState(true)
  const [rescoring, setRescoring] = useState(false)
  const [data, setData] = useState<ScoreResult | null>(null)
  const [expandedCards, setExpandedCards] = useState<Set<string>>(new Set())

  const loadData = async (isRescore = false) => {
    if (!batchId) return
    if (isRescore) setRescoring(true)
    else setLoading(true)

    try {
      const res = await scoreService.scoreBatch(batchId)
      setData(res.data)
      if (isRescore) toast.success('Đã chấm điểm lại thành công!')
    } catch (err) {
      toast.error('Lỗi khi tải kết quả chấm điểm')
      console.error(err)
    } finally {
      setLoading(false)
      setRescoring(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [batchId])

  const toggleExpand = (id: string) => {
    const next = new Set(expandedCards)
    if (next.has(id)) next.delete(id)
    else next.add(id)
    setExpandedCards(next)
  }

  const getLabelColor = (label: string) => {
    switch (label) {
      case 'Excellent': return '#4ade80'
      case 'Good': return '#38bdf8'
      case 'Fair': return '#fbbf24'
      case 'Poor': return '#f87171'
      default: return '#94a3b8'
    }
  }

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center h-full min-h-[400px]">
        <Loader2 size={40} className="animate-spin mb-4" style={{ color: 'var(--color-primary)' }} />
        <h2 className="text-xl font-bold gradient-text">Đang phân tích và chấm điểm CV...</h2>
        <p className="mt-2 text-sm" style={{ color: 'var(--color-text-muted)' }}>
          Hệ thống đang sử dụng Weaviate Vector DB để tính toán độ tương đồng ngữ nghĩa.
        </p>
      </div>
    )
  }

  if (!data) {
    return (
      <div className="space-y-6">
        <button onClick={() => navigate('/batches')} className="btn-ghost" style={{ padding: '0.4rem 0.8rem' }}>
          <ArrowLeft size={16} /> Quay lại
        </button>
        <div className="glass p-10 text-center">
          <p>Không có dữ liệu.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6 animate-fadeInUp">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <button onClick={() => navigate('/batches')} className="btn-ghost mb-3" style={{ padding: '0.4rem 0.8rem', fontSize: '0.8rem' }}>
            <ArrowLeft size={14} /> Quay lại Batch
          </button>
          <h1 className="text-3xl font-bold gradient-text">{data.batchName}</h1>
          <p className="mt-1 text-sm flex items-center gap-2" style={{ color: 'var(--color-text-muted)' }}>
            <Layers size={14} />
            Đã chấm điểm {data.totalCandidates} CV hoàn tất quá trình ETL
          </p>
        </div>
        <button onClick={() => loadData(true)} disabled={rescoring} className="btn-primary flex-shrink-0">
          <RefreshCw size={16} className={rescoring ? 'animate-spin' : ''} />
          {rescoring ? 'Đang chấm lại...' : 'Chấm lại'}
        </button>
      </div>

      {/* Stats Summary */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        {['Excellent', 'Good', 'Fair', 'Poor'].map(lbl => {
          const count = data.results.filter(r => r.label === lbl).length
          const color = getLabelColor(lbl)
          return (
            <div key={lbl} className="glass p-4 text-center">
              <p className="text-2xl font-bold" style={{ color }}>{count}</p>
              <p className="text-xs mt-1 font-medium text-slate-300">{lbl}</p>
            </div>
          )
        })}
      </div>

      {/* Candidate List */}
      <div className="space-y-4">
        {data.results.length === 0 ? (
          <div className="glass p-10 text-center text-slate-400">
            Chưa có CV nào trong Batch này hoàn thành quá trình ETL. Hãy Upload thêm CV.
          </div>
        ) : (
          data.results.map((cand, idx) => {
            const isExpanded = expandedCards.has(cand.resumeId)
            const labelColor = getLabelColor(cand.label)
            return (
              <div key={cand.resumeId} className="glass overflow-hidden transition-all duration-300"
                style={{ borderColor: cand.hasMissingMandatory ? 'rgba(239,68,68,0.3)' : undefined }}>
                {/* Card Header (Always visible) */}
                <div
                  className="p-5 flex flex-col md:flex-row md:items-center justify-between gap-4 cursor-pointer hover:bg-white/5 transition-colors"
                  onClick={() => toggleExpand(cand.resumeId)}
                >
                  <div className="flex items-center gap-4 min-w-[250px]">
                    <div className="w-10 h-10 rounded-full flex items-center justify-center font-bold text-lg"
                      style={{ background: `${labelColor}20`, color: labelColor, border: `1px solid ${labelColor}40` }}>
                      #{idx + 1}
                    </div>
                    <div>
                      <h3 className="font-bold text-lg">{cand.candidateName}</h3>
                      <div className="flex items-center gap-2 mt-1">
                        <span className="badge" style={{ background: `${labelColor}15`, color: labelColor, border: `1px solid ${labelColor}30` }}>
                          {cand.label}
                        </span>
                        {cand.hasMissingMandatory && (
                          <span className="badge badge-danger">Thiếu Mandatory Skill (-40%)</span>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Progress Bars */}
                  <div className="flex-1 grid grid-cols-3 gap-6 max-w-lg w-full">
                    <ProgressBar label="Semantic" score={cand.breakdown.semanticScore} color="#a78bfa" />
                    <ProgressBar label="Skill" score={cand.breakdown.skillScore} color="#38bdf8" />
                    <ProgressBar label="YoE" score={cand.breakdown.yoeScore} color="#fbbf24" />
                  </div>

                  {/* Final Score */}
                  <div className="flex items-center gap-4 text-right">
                    <div>
                      <p className="text-xs" style={{ color: 'var(--color-text-muted)' }}>Final Score</p>
                      <p className="text-3xl font-black gradient-text">{cand.finalScore.toFixed(1)}</p>
                    </div>
                    {isExpanded ? <ChevronUp size={20} className="text-slate-400" /> : <ChevronDown size={20} className="text-slate-400" />}
                  </div>
                </div>

                {/* Expanded Details */}
                {isExpanded && (
                  <div className="border-t p-5 bg-slate-900/50" style={{ borderColor: 'var(--color-border)' }}>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                      {/* Matched Skills */}
                      <div>
                        <h4 className="text-sm font-semibold mb-3 flex items-center gap-2 text-green-400">
                          <CheckCircle2 size={16} /> Kỹ năng đáp ứng ({cand.skillMatches.length})
                        </h4>
                        <div className="space-y-2">
                          {cand.skillMatches.length === 0 ? (
                            <p className="text-xs text-slate-500 italic">Không có kỹ năng nào khớp.</p>
                          ) : (
                            cand.skillMatches.map((sm, i) => (
                              <div key={i} className="flex items-center justify-between p-2 rounded bg-white/5 border border-white/5">
                                <div className="flex flex-col">
                                  <span className="text-sm font-medium">
                                    {sm.requiredSkill}
                                    {sm.isMandatory && <span className="text-red-400 ml-1 text-xs">*</span>}
                                  </span>
                                  <span className="text-xs text-slate-400">Match: {sm.matchedWith}</span>
                                </div>
                                <span className="text-xs font-mono text-green-400 bg-green-400/10 px-2 py-1 rounded">
                                  {(sm.certainty * 100).toFixed(0)}%
                                </span>
                              </div>
                            ))
                          )}
                        </div>
                      </div>

                      {/* Missing Skills */}
                      <div>
                        <h4 className="text-sm font-semibold mb-3 flex items-center gap-2 text-red-400">
                          <XCircle size={16} /> Kỹ năng còn thiếu ({cand.missingSkills.length})
                        </h4>
                        <div className="space-y-2">
                          {cand.missingSkills.length === 0 ? (
                            <p className="text-xs text-slate-500 italic">Đáp ứng đủ tất cả kỹ năng!</p>
                          ) : (
                            cand.missingSkills.map((sk, i) => {
                              const isMandatory = cand.missingMandatorySkills.includes(sk)
                              return (
                                <div key={i} className="flex items-center justify-between p-2 rounded bg-white/5 border border-white/5">
                                  <span className="text-sm">
                                    {sk}
                                    {isMandatory && <span className="badge badge-danger ml-2 text-[10px]">Bắt buộc</span>}
                                  </span>
                                  <span className="text-xs text-slate-500 italic">0%</span>
                                </div>
                              )
                            })
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            )
          })
        )}
      </div>
    </div>
  )
}

const ProgressBar: React.FC<{ label: string; score: number; color: string }> = ({ label, score, color }) => (
  <div>
    <div className="flex justify-between text-xs mb-1">
      <span className="text-slate-400">{label}</span>
      <span className="font-mono">{score.toFixed(0)}%</span>
    </div>
    <div className="h-1.5 rounded-full overflow-hidden bg-white/10">
      <div className="h-full rounded-full transition-all duration-500 ease-out"
        style={{ width: `${Math.max(score, 0)}%`, background: color }} />
    </div>
  </div>
)

export default BatchScoringPage

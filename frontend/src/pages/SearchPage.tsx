import React, { useState } from 'react'
import { Search, Loader2, Mail, Phone, Code, Sparkles, User } from 'lucide-react'
import { searchService } from '../services'

interface Candidate {
  resumeId: string;
  fullName: string;
  email: string;
  phone: string;
  skills: string[];
  summary: string;
  hybridScore: number;
}

const SearchPage: React.FC = () => {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<Candidate[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [hasSearched, setHasSearched] = useState(false)

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!query.trim()) return

    setIsLoading(true)
    setHasSearched(true)
    try {
      const response = await searchService.searchCandidates(query)
      setResults(response.data as Candidate[])
    } catch (error) {
      console.error("Search failed:", error)
      setResults([])
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
      {/* Header Area */}
      <div className="text-center space-y-4 max-w-2xl mx-auto pt-8">
        <h1 className="text-5xl font-extrabold tracking-tight">
          <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 via-purple-400 to-pink-400">
            Hybrid Search
          </span>
        </h1>
        <p className="text-lg text-slate-400">
          Tìm kiếm thông minh kết hợp Keyword (BM25) và Semantic (Vector) để tìm ứng viên phù hợp nhất.
        </p>
      </div>

      {/* Search Bar */}
      <div className="max-w-3xl mx-auto">
        <form onSubmit={handleSearch} className="relative group">
          <div className="absolute -inset-1 bg-gradient-to-r from-blue-500 to-purple-600 rounded-2xl blur opacity-25 group-hover:opacity-50 transition duration-1000 group-hover:duration-200" />
          <div className="relative flex items-center bg-[#1a1f2e] ring-1 ring-white/10 rounded-2xl shadow-2xl p-2">
            <div className="pl-4 pr-2 text-slate-400">
              <Search size={24} />
            </div>
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Ví dụ: Senior Java Developer with Spring Boot experience..."
              className="w-full bg-transparent border-none text-lg text-white placeholder-slate-500 focus:outline-none focus:ring-0 py-4"
            />
            <button
              type="submit"
              disabled={isLoading || !query.trim()}
              className="bg-gradient-to-r from-blue-500 to-purple-500 hover:from-blue-600 hover:to-purple-600 text-white font-semibold py-3 px-8 rounded-xl transition-all disabled:opacity-50 flex items-center gap-2"
            >
              {isLoading ? <Loader2 className="animate-spin" size={20} /> : <Sparkles size={20} />}
              Search
            </button>
          </div>
        </form>
      </div>

      {/* Results Area */}
      {hasSearched && (
        <div className="max-w-5xl mx-auto mt-12 space-y-6">
          {!isLoading && results.length === 0 && (
            <div className="text-center py-20 text-slate-400 glass rounded-3xl">
              <Search size={48} className="mx-auto mb-4 opacity-50" />
              <p className="text-xl">Không tìm thấy ứng viên nào phù hợp với yêu cầu.</p>
            </div>
          )}

          <div className="grid gap-6">
            {results.map((candidate, index) => (
              <div 
                key={candidate.resumeId}
                className="glass rounded-3xl p-6 hover:-translate-y-1 hover:shadow-2xl hover:shadow-blue-500/10 transition-all duration-300 relative overflow-hidden group"
                style={{ animationDelay: `${index * 100}ms` }}
              >
                {/* Score Badge */}
                <div className="absolute top-6 right-6 bg-gradient-to-br from-blue-500/20 to-purple-500/20 ring-1 ring-purple-500/30 px-4 py-2 rounded-xl backdrop-blur-md flex flex-col items-center">
                  <span className="text-xs text-purple-300 font-medium uppercase tracking-wider">Match Score</span>
                  <span className="text-2xl font-bold text-white">{(candidate.hybridScore * 100).toFixed(1)}</span>
                </div>

                <div className="flex gap-6">
                  {/* Avatar Placeholder */}
                  <div className="h-20 w-20 rounded-2xl bg-gradient-to-br from-slate-700 to-slate-800 ring-1 ring-white/10 flex items-center justify-center flex-shrink-0">
                    <User size={32} className="text-slate-400" />
                  </div>

                  <div className="flex-1 space-y-4">
                    <div>
                      <h3 className="text-2xl font-bold text-white group-hover:text-blue-400 transition-colors">
                        {candidate.fullName}
                      </h3>
                      <div className="flex gap-4 mt-2 text-slate-400 text-sm">
                        <span className="flex items-center gap-1.5"><Mail size={16} /> {candidate.email || 'N/A'}</span>
                        <span className="flex items-center gap-1.5"><Phone size={16} /> {candidate.phone || 'N/A'}</span>
                      </div>
                    </div>

                    <p className="text-slate-300 line-clamp-2 leading-relaxed">
                      {candidate.summary}
                    </p>

                    <div className="flex flex-wrap gap-2 pt-2">
                      {candidate.skills?.slice(0, 8).map(skill => (
                        <span key={skill} className="flex items-center gap-1.5 bg-blue-500/10 text-blue-300 ring-1 ring-blue-500/20 px-3 py-1 rounded-lg text-sm font-medium">
                          <Code size={14} />
                          {skill}
                        </span>
                      ))}
                      {candidate.skills?.length > 8 && (
                        <span className="bg-slate-800 text-slate-400 ring-1 ring-white/10 px-3 py-1 rounded-lg text-sm font-medium">
                          +{candidate.skills.length - 8} more
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

export default SearchPage

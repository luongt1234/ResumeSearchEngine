import api from '../lib/api'

export const authService = {
  login: (username: string, password: string) =>
    api.post<{ data: { token: string } }>('/auth/login', { username, password }),

  register: (username: string, password: string, email: string) =>
    api.post('/auth/register', { username, password, email }),
}

export const resumeService = {
  upload: (file: File, batchId?: string, userId?: string) => {
    const form = new FormData()
    form.append('file', file)
    if (batchId) form.append('batchId', batchId)
    return api.post('/cv/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data', ...(userId ? { 'X-User-Id': userId } : {}) },
    })
  },
}

export const batchService = {
  create: (data: { batchName: string; targetPosition: string; minYoe: number; description: string }) =>
    api.post('/batches', data),

  getAll: () => api.get('/batches'),

  getById: (id: string) => api.get(`/batches/${id}`),

  update: (id: string, data: { batchName: string; targetPosition: string; minYoe: number; description: string }) =>
    api.put(`/batches/${id}`, data),

  delete: (id: string) => api.delete(`/batches/${id}`),
}

export const skillService = {
  add: (batchId: string, data: { skillName: string; weight: number; isMandatory: boolean }) =>
    api.post(`/batches/${batchId}/skills`, data),

  getByBatch: (batchId: string) => api.get(`/batches/${batchId}/skills`),

  update: (batchId: string, skillId: string, data: { skillName: string; weight: number; isMandatory: boolean }) =>
    api.put(`/batches/${batchId}/skills/${skillId}`, data),

  remove: (batchId: string, skillId: string) => api.delete(`/batches/${batchId}/skills/${skillId}`),
}

export const scoreService = {
  scoreBatch: (batchId: string) => api.post(`/batches/${batchId}/score`),
}

export const searchService = {
  searchCandidates: (query: string) => api.get(`/search/candidates?q=${encodeURIComponent(query)}`),
}

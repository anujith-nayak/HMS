import api from './axios'

export const getPrescriptions   = (search = '') => api.get('/prescriptions', { params: search ? { search } : {} })
export const getPrescription    = (id)           => api.get(`/prescriptions/${id}`)
export const createPrescription = (data)         => api.post('/prescriptions', data)
export const updatePrescription = (id, data)     => api.put(`/prescriptions/${id}`, data)
export const deletePrescription = (id)           => api.delete(`/prescriptions/${id}`)

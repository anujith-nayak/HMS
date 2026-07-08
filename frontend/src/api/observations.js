import api from './axios'

export const getObservations   = (search = '') => api.get('/observations', { params: search ? { search } : {} })
export const getObservation    = (id)           => api.get(`/observations/${id}`)
export const createObservation = (data)         => api.post('/observations', data)
export const updateObservation = (id, data)     => api.put(`/observations/${id}`, data)
export const deleteObservation = (id)           => api.delete(`/observations/${id}`)

import api from './axios'

export const getAppointments   = (search = '') => api.get('/appointments', { params: search ? { search } : {} })
export const getAppointment    = (id)           => api.get(`/appointments/${id}`)
export const createAppointment = (data)         => api.post('/appointments', data)
export const updateAppointment = (id, data)     => api.put(`/appointments/${id}`, data)
export const deleteAppointment = (id)           => api.delete(`/appointments/${id}`)

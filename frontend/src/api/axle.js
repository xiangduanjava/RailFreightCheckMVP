import axios from 'axios'

const http = axios.create({ baseURL: '/api' })

export function listAxles(params) {
  return http.get('/axles', { params }).then((r) => r.data)
}

export function getAxle(id) {
  return http.get(`/axles/${id}`).then((r) => r.data)
}

export function updateAxleStatus(id, payload) {
  return http.put(`/axles/${id}/status`, payload).then((r) => r.data)
}

export function createAxle(payload) {
  return http.post('/axles/create', payload).then((r) => r.data)
}

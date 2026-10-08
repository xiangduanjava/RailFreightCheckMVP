import axios from 'axios'

const http = axios.create({ baseURL: '/api' })

export function submitDetection(payload) {
  return http.post('/detections', payload).then((r) => r.data)
}

export function listDetections(limit = 5) {
  return http.get('/detections', { params: { limit } }).then((r) => r.data)
}

export function getDetectionSummary() {
  return http.get('/detections/summary').then((r) => r.data)
}

import request from '@/utils/request'

export function getScenicSpots(params: any) {
  return request.get('/scenic-spots', { params })
}

export function getActiveSpots() {
  return request.get('/scenic-spots/active')
}

export function getScenicSpot(id: number) {
  return request.get(`/scenic-spots/${id}`)
}

export function createScenicSpot(data: any) {
  return request.post('/scenic-spots', data)
}

export function updateScenicSpot(id: number, data: any) {
  return request.put(`/scenic-spots/${id}`, data)
}

export function deleteScenicSpot(id: number) {
  return request.delete(`/scenic-spots/${id}`)
}

export function toggleScenicSpotStatus(id: number) {
  return request.put(`/scenic-spots/${id}/status`)
}

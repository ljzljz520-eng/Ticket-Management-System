import request from '@/utils/request'

export function getTicketTypes(params: any) {
  return request.get('/ticket-types', { params })
}

export function getActiveTicketTypes(scenicSpotId: number) {
  return request.get('/ticket-types/active', { params: { scenicSpotId } })
}

export function getTicketType(id: number) {
  return request.get(`/ticket-types/${id}`)
}

export function createTicketType(data: any) {
  return request.post('/ticket-types', data)
}

export function updateTicketType(id: number, data: any) {
  return request.put(`/ticket-types/${id}`, data)
}

export function deleteTicketType(id: number) {
  return request.delete(`/ticket-types/${id}`)
}

export function toggleTicketTypeStatus(id: number) {
  return request.put(`/ticket-types/${id}/status`)
}

export function adjustTicketStock(id: number, stock: number) {
  return request.put(`/ticket-types/${id}/stock`, { stock })
}

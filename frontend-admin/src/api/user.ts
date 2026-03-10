import request from '@/utils/request'

export function getUsers(params: any) {
  return request.get('/users', { params })
}

export function createUser(data: any) {
  return request.post('/users', data)
}

export function updateUser(id: number, data: any) {
  return request.put(`/users/${id}`, data)
}

export function deleteUser(id: number) {
  return request.delete(`/users/${id}`)
}

export function toggleUserStatus(id: number) {
  return request.put(`/users/${id}/status`)
}

export function resetPassword(id: number) {
  return request.put(`/users/${id}/reset-password`)
}

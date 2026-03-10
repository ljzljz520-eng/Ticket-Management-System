import request from '@/utils/request'

export interface LoginParams {
  username: string
  password: string
}

export interface UserInfo {
  token: string
  userId: number
  username: string
  realName: string
  role: string
  avatar: string
}

export function login(data: LoginParams) {
  return request.post('/auth/login', data)
}

export function logout() {
  return request.post('/auth/logout')
}

export function getUserInfo() {
  return request.get('/auth/info')
}

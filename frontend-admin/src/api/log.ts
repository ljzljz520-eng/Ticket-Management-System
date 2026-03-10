import request from '@/utils/request'

export function getLogs(params: any) {
  return request.get('/logs', { params })
}

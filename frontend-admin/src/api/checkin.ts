import request from '@/utils/request'

export function checkIn(data: { orderNo: string; gateNo?: string }) {
  return request.post('/check-in', data)
}

export function checkOut(id: number) {
  return request.put(`/check-in/${id}/check-out`)
}

export function getCheckInRecords(params: any) {
  return request.get('/check-in/records', { params })
}

export function getTodayCheckInStats() {
  return request.get('/check-in/today')
}

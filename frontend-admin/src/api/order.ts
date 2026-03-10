import request from '@/utils/request'

export function getOrders(params: any) {
  return request.get('/orders', { params })
}

export function getOrderDetail(id: number) {
  return request.get(`/orders/${id}`)
}

export function checkOrder(orderNo: string) {
  return request.get(`/orders/check/${orderNo}`)
}

export function createOrder(data: any) {
  return request.post('/orders', data)
}

export function payOrder(id: number) {
  return request.put(`/orders/${id}/pay`)
}

export function cancelOrder(id: number) {
  return request.put(`/orders/${id}/cancel`)
}

export function refundOrder(id: number) {
  return request.put(`/orders/${id}/refund`)
}

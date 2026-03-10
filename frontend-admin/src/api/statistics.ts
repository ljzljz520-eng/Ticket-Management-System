import request from '@/utils/request'

export function getOverview() {
  return request.get('/statistics/overview')
}

export function getSalesTrend(params?: any) {
  return request.get('/statistics/sales', { params })
}

export function getTicketTypeSales(params?: any) {
  return request.get('/statistics/ticket-types', { params })
}

export function getVisitorTrend(params?: any) {
  return request.get('/statistics/visitors', { params })
}

export function getScenicSpotComparison(params?: any) {
  return request.get('/statistics/scenic-spots', { params })
}

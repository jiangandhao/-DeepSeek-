import request from '@/utils/request'

// 搜索体检中心
export function searchCenters(data) {
  return request.post('/api/checkup/search-centers', data)
}

// 创建预约
export function createAppointment(data) {
  return request.post('/api/checkup/appointment', data)
}

// 获取预约列表
export function getAppointments(params) {
  return request.get('/api/checkup/appointments', { params })
}

// 取消预约
export function cancelAppointment(id) {
  return request.delete(`/api/checkup/appointment/${id}`)
}

// 获取体检报告列表
export function getReports(params) {
  return request.get('/api/checkup/reports', { params })
}

// 获取报告详情
export function getReportDetail(id) {
  return request.get(`/api/checkup/report/${id}`)
}

// 影像分析
export function analyzeImage(data) {
  return request.post('/api/checkup/image-analysis', data)
}

// 获取分析历史
export function getAnalysisHistory(params) {
  return request.get('/api/checkup/analysis-history', { params })
}

// 获取套餐列表
export function getPackages(params) {
  return request.get('/api/checkup/packages', { params })
}

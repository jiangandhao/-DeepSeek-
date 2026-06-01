import request from '@/utils/request'

// 获取风险评估结果
export function getRiskAssessment() {
  return request.get('/api/risk-warning/assessment')
}

// 刷新风险评估
export function refreshAssessment() {
  return request.post('/api/risk-warning/refresh-assessment')
}

// 获取预警列表
export function getWarnings(params) {
  return request.get('/api/risk-warning/warnings', { params })
}

// 标记预警已读
export function markWarningRead(id) {
  return request.put(`/api/risk-warning/warning/${id}/read`)
}

// 获取预警设置
export function getAlertSettings() {
  return request.get('/api/risk-warning/settings')
}

// 更新预警设置
export function updateAlertSettings(data) {
  return request.put('/api/risk-warning/settings', data)
}

// 获取预防方案列表
export function getPreventionPlans() {
  return request.get('/api/risk-warning/prevention-plans')
}

// 获取疾病预防方案详情
export function getPreventionPlanDetail(diseaseId) {
  return request.get(`/api/risk-warning/prevention-plan/${diseaseId}`)
}

// 导出预防方案
export function exportPreventionPlan(diseaseId) {
  return request.get(`/api/risk-warning/prevention-plan/${diseaseId}/export`, { responseType: 'blob' })
}

// 获取风险趋势数据
export function getRiskTrend(params) {
  return request.get('/api/risk-warning/trend', { params })
}

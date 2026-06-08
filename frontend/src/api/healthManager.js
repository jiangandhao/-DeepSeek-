import request from '@/utils/request'

// 健康风险评估
export function getHealthRiskAssessment(data) {
  return request({
    url: '/health-manager/risk-assessment',
    method: 'post',
    data
  })
}

// 获取风险评估历史
export function getAssessmentHistory(userId) {
  return request({
    url: `/health-manager/risk-assessment/history/${userId}`,
    method: 'get'
  })
}

// 生成饮食处方
export function getDietPrescription(data) {
  return request({
    url: '/health-manager/diet-prescription',
    method: 'post',
    data
  })
}

// 生成运动方案
export function getExercisePlan(data) {
  return request({
    url: '/health-manager/exercise-plan',
    method: 'post',
    data
  })
}

// 获取慢性病管理信息
export function getDiseaseManagement(userId) {
  return request({
    url: `/health-manager/disease-management/${userId}`,
    method: 'get'
  })
}

// 添加监测记录
export function addMonitoringRecord(data) {
  return request({
    url: '/health-manager/monitoring-record',
    method: 'post',
    data
  })
}

// 获取用药提醒
export function getMedicationReminder(userId) {
  return request({
    url: `/health-manager/medication-reminder/${userId}`,
    method: 'get'
  })
}

// 获取复查提醒
export function getFollowUpReminders(userId) {
  return request({
    url: `/health-manager/follow-up/${userId}`,
    method: 'get'
  })
}

// 获取AI健康建议
export function getAiAdvice(userId) {
  return request({
    url: `/health-manager/ai-advice/${userId}`,
    method: 'get'
  })
}

// 获取健康档案
export function getUserHealthProfile(userId) {
  return request({
    url: `/health-manager/profile/${userId}`,
    method: 'get'
  })
}

// 更新健康档案
export function updateUserHealthProfile(userId, data) {
  return request({
    url: `/health-manager/profile/${userId}`,
    method: 'put',
    data
  })
}

// 获取体重趋势数据
export function getWeightTrend(userId, period = '7d') {
  return request({
    url: `/health-manager/weight-trend/${userId}`,
    method: 'get',
    params: { period }
  })
}

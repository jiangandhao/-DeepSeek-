import request from '@/utils/request'

// 风险评估相关
export function getRiskAssessment(userId) {
  return request({
    url: `/risk/assessment/${userId}`,
    method: 'get'
  })
}

export function submitAssessment(data) {
  return request({
    url: '/risk/assessment',
    method: 'post',
    data
  })
}

export function getRiskDetail(userId, diseaseType) {
  return request({
    url: `/risk/assessment/${userId}/${diseaseType}`,
    method: 'get'
  })
}

// 风险分级
export function getRiskLevels(userId) {
  return request({
    url: `/risk/levels/${userId}`,
    method: 'get'
  })
}

export function getRiskFactors(userId) {
  return request({
    url: `/risk/factors/${userId}`,
    method: 'get'
  })
}

// 预警相关
export function getWarnings(userId, params) {
  return request({
    url: '/risk/warnings',
    method: 'get',
    params: { userId, ...params }
  })
}

export function markWarningRead(id) {
  return request({
    url: `/risk/warnings/${id}/read`,
    method: 'put'
  })
}

export function markAllWarningsRead(userId) {
  return request({
    url: '/risk/warnings/read-all',
    method: 'put',
    params: { userId }
  })
}

export function dismissWarning(id) {
  return request({
    url: `/risk/warnings/${id}/dismiss`,
    method: 'put'
  })
}

// 预警统计
export function getWarningStats(userId) {
  return request({
    url: `/risk/warnings/stats/${userId}`,
    method: 'get'
  })
}

// 预警设置
export function getWarningThresholds(userId) {
  return request({
    url: `/risk/thresholds/${userId}`,
    method: 'get'
  })
}

export function updateWarningThresholds(userId, data) {
  return request({
    url: `/risk/thresholds/${userId}`,
    method: 'put',
    data
  })
}

// 预防方案
export function getPreventionPlan(userId) {
  return request({
    url: `/risk/prevention-plan/${userId}`,
    method: 'get'
  })
}

export function generatePreventionPlan(data) {
  return request({
    url: '/risk/prevention-plan/generate',
    method: 'post',
    data
  })
}

// 健康目标
export function getHealthGoals(userId) {
  return request({
    url: `/risk/goals/${userId}`,
    method: 'get'
  })
}

export function updateHealthGoal(userId, goalId, data) {
  return request({
    url: `/risk/goals/${userId}/${goalId}`,
    method: 'put',
    data
  })
}

// 健康建议
export function getHealthAdvice(userId) {
  return request({
    url: `/risk/advice/${userId}`,
    method: 'get'
  })
}

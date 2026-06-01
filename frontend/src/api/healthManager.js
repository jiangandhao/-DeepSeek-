import request from '../utils/request'

export function getHealthRiskAssessment(data) {
  return request({
    url: '/health-manager/risk-assessment',
    method: 'post',
    data
  })
}

export function getDietPrescription(data) {
  return request({
    url: '/health-manager/diet-prescription',
    method: 'post',
    data
  })
}

export function getExercisePlan(data) {
  return request({
    url: '/health-manager/exercise-plan',
    method: 'post',
    data
  })
}

export function getDiseaseManagement(data) {
  return request({
    url: '/health-manager/disease-management',
    method: 'post',
    data
  })
}

export function getUserHealthProfile(userId) {
  return request({
    url: `/health-manager/profile/${userId}`,
    method: 'get'
  })
}

export function updateUserHealthProfile(userId, data) {
  return request({
    url: `/health-manager/profile/${userId}`,
    method: 'put',
    data
  })
}

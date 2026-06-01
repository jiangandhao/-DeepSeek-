import request from '../utils/request'

export function getBloodSugarRecords(params) {
  return request({
    url: '/blood-sugar/records',
    method: 'get',
    params
  })
}

export function addBloodSugarRecord(data) {
  return request({
    url: '/blood-sugar/records',
    method: 'post',
    data
  })
}

export function getBloodSugarTrend(params) {
  return request({
    url: '/blood-sugar/trend',
    method: 'get',
    params
  })
}

export function getDietRecommendation(data) {
  return request({
    url: '/blood-sugar/diet/recommendation',
    method: 'post',
    data
  })
}

export function getExerciseRecommendation(data) {
  return request({
    url: '/blood-sugar/exercise/recommendation',
    method: 'post',
    data
  })
}

export function getBloodSugarPrediction(params) {
  return request({
    url: '/blood-sugar/prediction',
    method: 'get',
    params
  })
}

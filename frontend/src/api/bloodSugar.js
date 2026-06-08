import request from '@/utils/request'

// 获取血糖记录列表
export function getBloodSugarRecords(userId, params) {
  return request({
    url: '/blood-sugar/records',
    method: 'get',
    params: { userId, ...params }
  })
}

// 添加血糖记录
export function addBloodSugarRecord(data) {
  return request({
    url: '/blood-sugar/records',
    method: 'post',
    data
  })
}

// 获取血糖趋势
export function getBloodSugarTrend(userId, period) {
  return request({
    url: '/blood-sugar/trend',
    method: 'get',
    params: { userId, period }
  })
}

// 获取饮食推荐
export function getDietRecommendation(data) {
  return request({
    url: '/blood-sugar/diet/recommendation',
    method: 'post',
    data
  })
}

// 获取运动推荐
export function getExerciseRecommendation(data) {
  return request({
    url: '/blood-sugar/exercise/recommendation',
    method: 'post',
    data
  })
}

// 获取血糖预测
export function getBloodSugarPrediction(userId) {
  return request({
    url: '/blood-sugar/prediction',
    method: 'get',
    params: { userId }
  })
}

// ========== 饮食记录 ==========

// 获取饮食记录列表
export function getDietRecords(userId, params) {
  return request({
    url: '/diet/records',
    method: 'get',
    params: { userId, ...params }
  })
}

// 添加饮食记录
export function addDietRecord(data) {
  return request({
    url: '/diet/records',
    method: 'post',
    data
  })
}

// 更新饮食记录
export function updateDietRecord(id, data) {
  return request({
    url: `/diet/records/${id}`,
    method: 'put',
    data
  })
}

// 删除饮食记录
export function deleteDietRecord(id) {
  return request({
    url: `/diet/records/${id}`,
    method: 'delete'
  })
}

// ========== 运动记录 ==========

// 获取运动记录列表
export function getExerciseRecords(userId, params) {
  return request({
    url: '/exercise/records',
    method: 'get',
    params: { userId, ...params }
  })
}

// 添加运动记录
export function addExerciseRecord(data) {
  return request({
    url: '/exercise/records',
    method: 'post',
    data
  })
}

// 更新运动记录
export function updateExerciseRecord(id, data) {
  return request({
    url: `/exercise/records/${id}`,
    method: 'put',
    data
  })
}

// 删除运动记录
export function deleteExerciseRecord(id) {
  return request({
    url: `/exercise/records/${id}`,
    method: 'delete'
  })
}

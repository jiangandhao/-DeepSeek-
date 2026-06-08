import request from '@/utils/request'

// 获取血压记录列表
export function getBloodPressureRecords(userId) {
  return request({
    url: '/blood-pressure/records',
    method: 'get',
    params: { userId }
  })
}

// 获取血压趋势数据
export function getBloodPressureTrend(userId, period = '7d') {
  return request({
    url: '/blood-pressure/trend',
    method: 'get',
    params: { userId, period }
  })
}

// 添加血压记录
export function addBloodPressureRecord(data) {
  return request({
    url: '/blood-pressure/records',
    method: 'post',
    data
  })
}

// 删除血压记录
export function deleteBloodPressureRecord(id) {
  return request({
    url: `/blood-pressure/records/${id}`,
    method: 'delete'
  })
}

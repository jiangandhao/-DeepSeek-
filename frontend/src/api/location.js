import request from '@/utils/request'

// 获取附近医院列表
export function getNearbyHospitals(lat, lng, radius = 5) {
  return request({
    url: '/location/nearby-hospitals',
    method: 'get',
    params: { lat, lng, radius }
  })
}

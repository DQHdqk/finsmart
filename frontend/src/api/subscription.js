import request from '@/utils/request'

// 获取订阅列表
export const getSubscriptionList = () => {
  return request({
    url: '/subscription/list',
    method: 'get'
  })
}

// 新增订阅
export const addSubscription = (data) => {
  return request({
    url: '/subscription',
    method: 'post',
    data
  })
}

// 更新订阅
export const updateSubscription = (data) => {
  return request({
    url: '/subscription',
    method: 'put',
    data
  })
}

// 删除订阅
export const deleteSubscription = (id) => {
  return request({
    url: `/subscription/${id}`,
    method: 'delete'
  })
}

// 获取即将到期的订阅
export const getUpcomingSubscriptions = () => {
  return request({
    url: '/subscription/upcoming',
    method: 'get'
  })
}

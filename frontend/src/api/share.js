import request from '@/utils/request'

// 获取AA共享列表
export const getShareList = () => {
  return request({
    url: '/share/list',
    method: 'get'
  })
}

// 创建AA共享
export const createShare = (data) => {
  return request({
    url: '/share/create',
    method: 'post',
    data
  })
}

// 获取AA共享详情
export const getShareDetail = (id) => {
  return request({
    url: `/share/detail/${id}`,
    method: 'get'
  })
}

// 标记成员已付款
// 改成传body而不是路径参数
export const markMemberPaid = (data) => {
  return request({
    url: '/share/pay',
    method: 'put',
    data
  })
}

// 删除AA共享
export const deleteShare = (id) => {
  return request({
    url: `/share/${id}`,
    method: 'delete'
  })
}

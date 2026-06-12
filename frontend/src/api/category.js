import request from '@/utils/request'

// 获取分类列表
export const getCategoryList = (type) => {
  return request({
    url: '/category/list',
    method: 'get',
    params: type ? { type } : {}
  })
}

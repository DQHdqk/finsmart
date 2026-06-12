import request from '@/utils/request'

// 获取愿望清单
export const getWishList = () => {
  return request({
    url: '/wish/list',
    method: 'get'
  })
}

// 新增愿望
export const addWish = (data) => {
  return request({
    url: '/wish',
    method: 'post',
    data
  })
}

// 删除愿望
export const deleteWish = (id) => {
  return request({
    url: `/wish/${id}`,
    method: 'delete'
  })
}

// 存入金额到愿望
export const saveToWish = (id, amount) => {
  return request({
    url: `/wish/save/${id}`,
    method: 'put',
    params: { amount }
  })
}

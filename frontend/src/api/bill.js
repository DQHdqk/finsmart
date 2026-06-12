import request from '@/utils/request'

// 获取账单分页列表
export const getBillPage = (params) => {
  return request({
    url: '/bill/page',
    method: 'get',
    params
  })
}

// 新增账单
export const addBill = (data) => {
  return request({
    url: '/bill',
    method: 'post',
    data
  })
}

// 更新账单
export const updateBill = (data) => {
  return request({
    url: '/bill',
    method: 'put',
    data
  })
}

// 删除账单
export const deleteBill = (id) => {
  return request({
    url: `/bill/${id}`,
    method: 'delete'
  })
}

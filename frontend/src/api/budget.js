import request from '@/utils/request'

// 获取预算列表
export const getBudgetList = (month) => {
  return request({
    url: '/budget/list',
    method: 'get',
    params: { month }
  })
}

// 新建预算
export const addBudget = (data) => {
  return request({
    url: '/budget',
    method: 'post',
    data
  })
}

// 删除预算
export const deleteBudget = (id) => {
  return request({
    url: `/budget/${id}`,
    method: 'delete'
  })
}

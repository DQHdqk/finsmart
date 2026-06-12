import request from '@/utils/request'

// 获取月度统计数据
export const getMonthStatistics = (month) => {
  return request({
    url: '/statistics/month',
    method: 'get',
    params: { month }
  })
}

// 获取今日统计
export const getTodayStatistics = () => {
  return request({
    url: '/statistics/today',
    method: 'get'
  })
}

// 获取趋势数据
export const getStatisticsTrend = (type) => {
  return request({
    url: '/statistics/trend',
    method: 'get',
    params: { type }
  })
}

// 获取报告数据
export const getStatisticsReport = (type) => {
  return request({
    url: '/statistics/report',
    method: 'get',
    params: { type }
  })
}

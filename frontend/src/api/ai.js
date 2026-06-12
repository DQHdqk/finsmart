import request from '@/utils/request'

// 获取AI分析结果
export const getAiAnalysis = (month) => {
  return request({
    url: '/ai/analyze',
    method: 'get',
    params: { month }
  })
}

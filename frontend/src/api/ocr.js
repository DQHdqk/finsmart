import request from '@/utils/request'

// 文字智能记账
export const textOCR = (text) => {
  return request({
    url: '/ocr/text',
    method: 'post',
    params: { text }
  })
}

// 图片识别记账
export const imageOCR = (formData) => {
  return request({
    url: '/ocr/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

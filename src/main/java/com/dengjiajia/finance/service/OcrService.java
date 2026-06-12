package com.dengjiajia.finance.service;

import com.dengjiajia.finance.vo.OcrResultVO;

public interface OcrService {
    OcrResultVO recognizeBill(String imageBase64, Long userId);
    OcrResultVO parseTextToBill(String text, Long userId);
}


package com.dengjiajia.finance.controller;

import com.dengjiajia.finance.common.Result;
import com.dengjiajia.finance.common.UserContext;
import com.dengjiajia.finance.service.OcrService;
import com.dengjiajia.finance.vo.OcrResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@RestController
@RequestMapping("/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;

    /**
     * 图片识别记账
     */
    @PostMapping("/image")
    public Result<OcrResultVO> recognizeImage(@RequestParam("file") MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            OcrResultVO result = ocrService.recognizeBill(base64, UserContext.getUserId());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("图片识别失败：" + e.getMessage());
        }
    }

    /**
     * 文字智能记账
     */
    @PostMapping("/text")
    public Result<OcrResultVO> recognizeText(@RequestParam("text") String text) {
        OcrResultVO result = ocrService.parseTextToBill(text, UserContext.getUserId());
        return Result.success(result);
    }
}


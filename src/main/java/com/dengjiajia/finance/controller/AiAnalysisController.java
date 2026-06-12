package com.dengjiajia.finance.controller;

import com.dengjiajia.finance.common.Result;
import com.dengjiajia.finance.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;

    @GetMapping("/analyze")
    public Result<String> analyze(
            @RequestParam(required = false) String month) {
        String result = aiAnalysisService.analyzeMonth(month);
        return Result.success(result);
    }
}


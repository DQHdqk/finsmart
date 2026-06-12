package com.dengjiajia.finance.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OcrResultVO {
    private BigDecimal amount;      // 识别出的金额
    private String categoryName;    // 识别出的分类名
    private Long categoryId;        // 匹配到的分类ID
    private String remark;          // 识别出的备注
    private Integer type;           // 1支出 2收入
    private String rawText;         // AI原始识别文字
}


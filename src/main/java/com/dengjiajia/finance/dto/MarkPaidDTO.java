package com.dengjiajia.finance.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MarkPaidDTO {
    private Long memberId;
    private BigDecimal paidAmount;
    private String paidTime;    // 字符串格式 "yyyy-MM-dd HH:mm"
    private String paidRemark;
}

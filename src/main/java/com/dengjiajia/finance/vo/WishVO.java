package com.dengjiajia.finance.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class WishVO {
    private Long id;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal savedAmount;
    private BigDecimal dailySave;
    private BigDecimal remainAmount;   // 剩余金额
    private Double percentage;         // 完成百分比
    private Integer estimatedDays;     // 预计还需天数
    private LocalDate estimatedDate;   // 预计达成日期
    private String progressText;       // 展示文字
    private String icon;
    private String color;
    private String remark;
    private Integer status;
}


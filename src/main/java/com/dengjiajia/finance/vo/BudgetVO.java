package com.dengjiajia.finance.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BudgetVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private BigDecimal amount;      // 预算金额
    private BigDecimal usedAmount;  // 已使用金额
    private BigDecimal remainAmount; // 剩余金额
    private Double percentage;      // 使用百分比
    private String month;
    private Boolean isOverBudget;   // 是否超支
}


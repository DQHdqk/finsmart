package com.dengjiajia.finance.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BudgetDTO {
    private Long categoryId; // 为空表示总预算
    private BigDecimal amount;
    private String month;
}

package com.dengjiajia.finance.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TodayStatVO {
    private BigDecimal todayExpense;
    private BigDecimal todayIncome;
    private BigDecimal dailyAvg;
}



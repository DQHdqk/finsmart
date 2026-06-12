package com.dengjiajia.finance.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ReportVO {
    private String period;              // 周期描述
    private BigDecimal totalExpense;
    private BigDecimal totalIncome;
    private BigDecimal balance;
    private BigDecimal dailyAvg;
    private List<StatisticsVO.CategoryStatVO> categoryStats;
    private String aiAnalysis;          // AI分析文字
}

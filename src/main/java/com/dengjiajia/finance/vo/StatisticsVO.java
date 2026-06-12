package com.dengjiajia.finance.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class StatisticsVO {

    // 总收入
    private BigDecimal totalIncome;

    // 总支出
    private BigDecimal totalExpense;

    // 结余
    private BigDecimal balance;

    // 分类统计列表
    private List<CategoryStatVO> categoryStats;

    @Data
    public static class CategoryStatVO {
        private Long categoryId;
        private String categoryName;
        private String categoryIcon;
        private String categoryColor;
        private Integer type;
        private BigDecimal amount;
        private Double percentage; // 占比
    }
}

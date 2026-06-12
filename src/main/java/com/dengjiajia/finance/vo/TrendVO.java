package com.dengjiajia.finance.vo;

import lombok.Data;
import java.util.List;

@Data
public class TrendVO {
    private List<String> dates;         // 日期标签
    private List<Double> expenses;      // 每日支出
    private List<Double> incomes;       // 每日收入
}


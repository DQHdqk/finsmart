package com.dengjiajia.finance.service;

import com.dengjiajia.finance.vo.ReportVO;
import com.dengjiajia.finance.vo.StatisticsVO;
import com.dengjiajia.finance.vo.TodayStatVO;
import com.dengjiajia.finance.vo.TrendVO;

public interface StatisticsService {
    StatisticsVO getMonthStatistics(String month);
    TodayStatVO getTodayStat();
    TrendVO getTrend(String type);
    ReportVO getReport(String type);
}



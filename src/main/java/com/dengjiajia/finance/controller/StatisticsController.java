package com.dengjiajia.finance.controller;

import com.dengjiajia.finance.common.Result;
import com.dengjiajia.finance.service.StatisticsService;
import com.dengjiajia.finance.vo.ReportVO;
import com.dengjiajia.finance.vo.StatisticsVO;
import com.dengjiajia.finance.vo.TodayStatVO;
import com.dengjiajia.finance.vo.TrendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@Tag(name = "统计分析", description = "财务数据统计和AI报告分析")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/month")
    @Operation(summary = "获取月度统计", description = "获取指定月份的财务统计数据")
    public Result<StatisticsVO> getMonthStatistics(
            @Parameter(description = "月份，格式：YYYY-MM，不传则默认当前月") @RequestParam(required = false) String month) {
        return Result.success(statisticsService.getMonthStatistics(month));
    }
    
    @GetMapping("/today")
    @Operation(summary = "获取今日统计", description = "获取今日的财务统计数据")
    public Result<TodayStatVO> getTodayStat() {
        return Result.success(statisticsService.getTodayStat());
    }

    @GetMapping("/trend")
    @Operation(summary = "获取趋势数据", description = "获取收支趋势数据")
    public Result<TrendVO> getTrend(
            @Parameter(description = "趋势类型：week/month/year") @RequestParam(defaultValue = "month") String type) {
        return Result.success(statisticsService.getTrend(type));
    }

    @GetMapping("/report")
    @Operation(summary = "获取AI报告", description = "获取AI财务分析报告")
    public Result<ReportVO> getReport(
            @Parameter(description = "报告类型：week/month/year") @RequestParam(defaultValue = "month") String type) {
        return Result.success(statisticsService.getReport(type));
    }

}


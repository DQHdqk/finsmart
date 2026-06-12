package com.dengjiajia.finance.service.impl;

import com.dengjiajia.finance.common.UserContext;
import com.dengjiajia.finance.service.AiAnalysisService;
import com.dengjiajia.finance.service.StatisticsService;
import com.dengjiajia.finance.utils.AiUtil;
import com.dengjiajia.finance.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private final StatisticsService statisticsService;
    private final AiUtil aiUtil;

    @Override
    public String analyzeMonth(String month) {
        // 获取当月统计数据
        StatisticsVO stats = statisticsService.getMonthStatistics(month);

        // 构建给AI的提示词
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的个人财务分析师，请根据以下数据给出简洁的财务分析建议：\n\n");
        prompt.append("分析月份：").append(month).append("\n");
        prompt.append("总收入：").append(stats.getTotalIncome()).append("元\n");
        prompt.append("总支出：").append(stats.getTotalExpense()).append("元\n");
        prompt.append("结余：").append(stats.getBalance()).append("元\n\n");
        prompt.append("各分类支出明细：\n");

        stats.getCategoryStats().forEach(cat -> {
            prompt.append("- ").append(cat.getCategoryName())
                    .append("：").append(cat.getAmount()).append("元")
                    .append("（占").append(String.format("%.1f", cat.getPercentage())).append("%）\n");
        });

        prompt.append("\n请从以下几个方面给出分析（100-200字）：\n");
        prompt.append("1. 本月消费总体评价\n");
        prompt.append("2. 消费结构是否合理\n");
        prompt.append("3. 下月节省建议\n");

        return aiUtil.chat(prompt.toString());
    }
}


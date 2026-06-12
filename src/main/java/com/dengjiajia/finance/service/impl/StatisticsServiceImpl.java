package com.dengjiajia.finance.service.impl;

import com.dengjiajia.finance.common.UserContext;
import com.dengjiajia.finance.entity.Bill;
import com.dengjiajia.finance.entity.Category;
import com.dengjiajia.finance.service.BillService;
import com.dengjiajia.finance.service.CategoryService;
import com.dengjiajia.finance.service.StatisticsService;
import com.dengjiajia.finance.utils.AiUtil;
import com.dengjiajia.finance.vo.ReportVO;
import com.dengjiajia.finance.vo.StatisticsVO;
import com.dengjiajia.finance.vo.TodayStatVO;
import com.dengjiajia.finance.vo.TrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final BillService billService;
    private final CategoryService categoryService;
    private final AiUtil aiUtil;

    @Override
    public StatisticsVO getMonthStatistics(String month) {
        Long userId = UserContext.getUserId();

        List<Bill> bills = billService.lambdaQuery()
                .eq(Bill::getUserId, userId)
                .apply(month != null,
                        "DATE_FORMAT(bill_date,'%Y-%m') = {0}", month)
                .list();

        BigDecimal totalIncome = bills.stream()
                .filter(b -> b.getType() == 2)
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = bills.stream()
                .filter(b -> b.getType() == 1)
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, BigDecimal> categoryAmountMap = bills.stream()
                .collect(Collectors.groupingBy(
                        Bill::getCategoryId,
                        Collectors.reducing(BigDecimal.ZERO,
                                Bill::getAmount, BigDecimal::add)
                ));

        List<StatisticsVO.CategoryStatVO> categoryStats = categoryAmountMap
                .entrySet().stream().map(entry -> {
                    StatisticsVO.CategoryStatVO stat = new StatisticsVO.CategoryStatVO();
                    stat.setCategoryId(entry.getKey());
                    stat.setAmount(entry.getValue());

                    Category category = categoryService.getById(entry.getKey());
                    if (category != null) {
                        stat.setCategoryName(category.getName());
                        stat.setCategoryIcon(category.getIcon());
                        stat.setCategoryColor(category.getColor());
                        stat.setType(category.getType());
                    }

                    BigDecimal total = stat.getType() != null && stat.getType() == 2
                            ? totalIncome : totalExpense;
                    if (total.compareTo(BigDecimal.ZERO) > 0) {
                        double percentage = entry.getValue()
                                .divide(total, 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"))
                                .doubleValue();
                        stat.setPercentage(percentage);
                    } else {
                        stat.setPercentage(0.0);
                    }
                    return stat;
                })
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .collect(Collectors.toList());

        StatisticsVO vo = new StatisticsVO();
        vo.setTotalIncome(totalIncome);
        vo.setTotalExpense(totalExpense);
        vo.setBalance(totalIncome.subtract(totalExpense));
        vo.setCategoryStats(categoryStats);
        return vo;
    }

    @Override
    public TodayStatVO getTodayStat() {
        Long userId = UserContext.getUserId();
        LocalDate today = LocalDate.now();
        String thisMonth = today.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));

        List<Bill> todayBills = billService.lambdaQuery()
                .eq(Bill::getUserId, userId)
                .eq(Bill::getBillDate, today)
                .list();

        BigDecimal todayExpense = todayBills.stream()
                .filter(b -> b.getType() == 1)
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal todayIncome = todayBills.stream()
                .filter(b -> b.getType() == 2)
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StatisticsVO monthStat = getMonthStatistics(thisMonth);
        int dayOfMonth = today.getDayOfMonth();
        BigDecimal dailyAvg = dayOfMonth > 0
                ? monthStat.getTotalExpense().divide(
                new BigDecimal(dayOfMonth), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        TodayStatVO vo = new TodayStatVO();
        vo.setTodayExpense(todayExpense);
        vo.setTodayIncome(todayIncome);
        vo.setDailyAvg(dailyAvg);
        return vo;
    }

    @Override
    public TrendVO getTrend(String type) {
        Long userId = UserContext.getUserId();
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        java.time.format.DateTimeFormatter formatter;

        switch (type) {
            case "week":
                startDate = today.minusDays(6);
                formatter = java.time.format.DateTimeFormatter.ofPattern("MM-dd");
                break;
            case "year":
                startDate = today.minusMonths(11).withDayOfMonth(1);
                formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM");
                break;
            default:
                startDate = today.withDayOfMonth(1);
                formatter = java.time.format.DateTimeFormatter.ofPattern("MM-dd");
                break;
        }

        List<Bill> bills = billService.lambdaQuery()
                .eq(Bill::getUserId, userId)
                .ge(Bill::getBillDate, startDate)
                .le(Bill::getBillDate, today)
                .list();

        java.util.TreeMap<String, double[]> dateMap = new java.util.TreeMap<>();

        if ("year".equals(type)) {
            for (int i = 11; i >= 0; i--) {
                String key = today.minusMonths(i).format(formatter);
                dateMap.put(key, new double[]{0, 0});
            }
        } else {
            for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
                dateMap.put(d.format(formatter), new double[]{0, 0});
            }
        }

        for (Bill bill : bills) {
            String key = bill.getBillDate().format(formatter);
            if (dateMap.containsKey(key)) {
                if (bill.getType() == 1) dateMap.get(key)[0] += bill.getAmount().doubleValue();
                else dateMap.get(key)[1] += bill.getAmount().doubleValue();
            }
        }

        TrendVO vo = new TrendVO();
        vo.setDates(new java.util.ArrayList<>(dateMap.keySet()));
        vo.setExpenses(dateMap.values().stream()
                .map(v -> v[0]).collect(Collectors.toList()));
        vo.setIncomes(dateMap.values().stream()
                .map(v -> v[1]).collect(Collectors.toList()));
        return vo;
    }

    @Override
    public ReportVO getReport(String type) {
        Long userId = UserContext.getUserId();
        LocalDate today = LocalDate.now();
        String period;
        String month = today.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));

        switch (type) {
            case "week": period = "本周"; break;
            case "year": period = "本年"; break;
            default: period = "本月"; break;
        }

        StatisticsVO stats = getMonthStatistics(month);
        int days = today.getDayOfMonth();

        ReportVO vo = new ReportVO();
        vo.setPeriod(period);
        vo.setTotalExpense(stats.getTotalExpense());
        vo.setTotalIncome(stats.getTotalIncome());
        vo.setBalance(stats.getBalance());
        vo.setDailyAvg(days > 0
                ? stats.getTotalExpense().divide(new BigDecimal(days),
                2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        vo.setCategoryStats(stats.getCategoryStats());

        StringBuilder prompt = new StringBuilder();
        prompt.append(period).append("财务报告数据：\n");
        prompt.append("总支出：").append(stats.getTotalExpense()).append("元\n");
        prompt.append("总收入：").append(stats.getTotalIncome()).append("元\n");
        prompt.append("结余：").append(stats.getBalance()).append("元\n");
        prompt.append("日均支出：").append(vo.getDailyAvg()).append("元\n");
        prompt.append("请生成一份简洁的财务分析报告（150字以内），包含消费评价和建议。");

        vo.setAiAnalysis(aiUtil.chat(prompt.toString()));
        return vo;
    }
}


package com.dengjiajia.finance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dengjiajia.finance.common.BusinessException;
import com.dengjiajia.finance.common.UserContext;
import com.dengjiajia.finance.dto.BudgetDTO;
import com.dengjiajia.finance.entity.Bill;
import com.dengjiajia.finance.entity.Budget;
import com.dengjiajia.finance.entity.Category;
import com.dengjiajia.finance.mapper.BudgetMapper;
import com.dengjiajia.finance.service.BillService;
import com.dengjiajia.finance.service.BudgetService;
import com.dengjiajia.finance.service.CategoryService;
import com.dengjiajia.finance.vo.BudgetVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl extends ServiceImpl<BudgetMapper, Budget>
        implements BudgetService {

    private final CategoryService categoryService;
    private final BillService billService;

    @Override
    public void saveOrUpdate(BudgetDTO dto) {
        Long userId = UserContext.getUserId();
        // 查询是否已有该月该分类的预算
        Budget existing = this.lambdaQuery()
                .eq(Budget::getUserId, userId)
                .eq(Budget::getMonth, dto.getMonth())
                .eq(dto.getCategoryId() != null,
                        Budget::getCategoryId, dto.getCategoryId())
                .isNull(dto.getCategoryId() == null, Budget::getCategoryId)
                .one();

        if (existing != null) {
            // 更新
            existing.setAmount(dto.getAmount());
            this.updateById(existing);
        } else {
            // 新增
            Budget budget = new Budget();
            BeanUtils.copyProperties(dto, budget);
            budget.setUserId(userId);
            this.save(budget);
        }
    }

    @Override
    public List<BudgetVO> list(String month) {
        Long userId = UserContext.getUserId();
        List<Budget> budgets = this.lambdaQuery()
                .eq(Budget::getUserId, userId)
                .eq(Budget::getMonth, month)
                .list();

        return budgets.stream().map(budget -> {
            BudgetVO vo = new BudgetVO();
            BeanUtils.copyProperties(budget, vo);

            // 填充分类信息
            if (budget.getCategoryId() != null) {
                Category category = categoryService.getById(budget.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getName());
                    vo.setCategoryIcon(category.getIcon());
                    vo.setCategoryColor(category.getColor());
                }
            } else {
                vo.setCategoryName("总预算");
            }

            // 计算已使用金额
            List<Bill> bills = billService.lambdaQuery()
                    .eq(Bill::getUserId, userId)
                    .eq(Bill::getType, 1) // 只统计支出
                    .eq(budget.getCategoryId() != null,
                            Bill::getCategoryId, budget.getCategoryId())
                    .apply("DATE_FORMAT(bill_date,'%Y-%m') = {0}", month)
                    .list();

            BigDecimal usedAmount = bills.stream()
                    .map(Bill::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            vo.setUsedAmount(usedAmount);
            vo.setRemainAmount(budget.getAmount().subtract(usedAmount));
            vo.setIsOverBudget(usedAmount.compareTo(budget.getAmount()) > 0);

            // 计算使用百分比
            if (budget.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                double percentage = usedAmount
                        .divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .doubleValue();
                vo.setPercentage(Math.min(percentage, 100.0));
            } else {
                vo.setPercentage(0.0);
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        Budget budget = this.getById(id);
        if (budget == null || !budget.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("预算不存在");
        }
        this.removeById(id);
    }
}



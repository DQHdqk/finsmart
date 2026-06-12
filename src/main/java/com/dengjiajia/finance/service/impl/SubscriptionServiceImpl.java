package com.dengjiajia.finance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dengjiajia.finance.common.BusinessException;
import com.dengjiajia.finance.common.UserContext;
import com.dengjiajia.finance.dto.SubscriptionDTO;
import com.dengjiajia.finance.entity.Subscription;
import com.dengjiajia.finance.mapper.SubscriptionMapper;
import com.dengjiajia.finance.service.SubscriptionService;
import com.dengjiajia.finance.vo.SubscriptionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl extends ServiceImpl<SubscriptionMapper, Subscription>
        implements SubscriptionService {

    @Override
    public void add(SubscriptionDTO dto) {
        Subscription subscription = new Subscription();
        BeanUtils.copyProperties(dto, subscription);
        subscription.setUserId(UserContext.getUserId());
        subscription.setStatus(1);

        // ✅ 用startDate + cycle 计算nextDate
        if (subscription.getNextDate() == null) {
            LocalDate startDate = dto.getStartDate() != null ? dto.getStartDate() : LocalDate.now();
            String cycle = dto.getCycle() != null ? dto.getCycle().toLowerCase() : "monthly";

            LocalDate nextDate;
            switch (cycle) {
                case "quarterly":
                    nextDate = startDate.plusMonths(3);
                    break;
                case "semi_annually":
                    nextDate = startDate.plusMonths(6);
                    break;
                case "annually":
                    nextDate = startDate.plusYears(1);
                    break;
                default: // monthly
                    nextDate = startDate.plusMonths(1);
                    break;
            }
            subscription.setNextDate(nextDate);
        }

        if (subscription.getCycle() != null) {
            subscription.setCycle(subscription.getCycle().toLowerCase());
        }
        this.save(subscription);
    }

    @Override
    public void update(SubscriptionDTO dto) {
        if (dto.getId() == null) throw new BusinessException("ID不能为空");
        Subscription subscription = this.getById(dto.getId());
        if (subscription == null || !subscription.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("订阅不存在");
        }
        BeanUtils.copyProperties(dto, subscription);

        // ✅ 重新计算nextDate（和add()一样的逻辑）
        LocalDate startDate = dto.getStartDate() != null ? dto.getStartDate() : LocalDate.now();
        String cycle = dto.getCycle() != null ? dto.getCycle().toLowerCase() : "monthly";
        LocalDate nextDate;
        switch (cycle) {
            case "quarterly":   nextDate = startDate.plusMonths(3); break;
            case "semi_annually": nextDate = startDate.plusMonths(6); break;
            case "annually":    nextDate = startDate.plusYears(1); break;
            default:            nextDate = startDate.plusMonths(1); break;
        }
        subscription.setNextDate(nextDate);

        // ✅ cycle统一转小写
        if (subscription.getCycle() != null) {
            subscription.setCycle(subscription.getCycle().toLowerCase());
        }

        this.updateById(subscription);
    }

    @Override
    public void delete(Long id) {
        Subscription subscription = this.getById(id);
        if (subscription == null || !subscription.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("订阅不存在");
        }
        this.removeById(id);
    }

    @Override
    public List<SubscriptionVO> listVO() {
        List<Subscription> list = this.lambdaQuery()
                .eq(Subscription::getUserId, UserContext.getUserId())
                .orderByAsc(Subscription::getNextDate)
                .list();
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionVO> upcoming() {
        LocalDate now = LocalDate.now();
        LocalDate sevenDaysLater = now.plusDays(7);
        List<Subscription> list = this.lambdaQuery()
                .eq(Subscription::getUserId, UserContext.getUserId())
                .eq(Subscription::getStatus, 1)
                .between(Subscription::getNextDate, now, sevenDaysLater)
                .orderByAsc(Subscription::getNextDate)
                .list();
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    private SubscriptionVO toVO(Subscription s) {
        SubscriptionVO vo = new SubscriptionVO();
        BeanUtils.copyProperties(s, vo);
        long days = ChronoUnit.DAYS.between(LocalDate.now(), s.getNextDate());
        System.out.println("=== 订阅:" + s.getName() + " nextDate:" + s.getNextDate() + " days:" + days);
        vo.setDaysUntilNext((int) days);
        vo.setIsUrgent(days <= 7);
        vo.setCycleText("monthly".equals(s.getCycle()) ? "月付" : "年付");
        return vo;
    }
}



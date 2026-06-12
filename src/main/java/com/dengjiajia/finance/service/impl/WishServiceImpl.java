package com.dengjiajia.finance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dengjiajia.finance.common.BusinessException;
import com.dengjiajia.finance.common.UserContext;
import com.dengjiajia.finance.dto.WishDTO;
import com.dengjiajia.finance.entity.Wish;
import com.dengjiajia.finance.mapper.WishMapper;
import com.dengjiajia.finance.service.WishService;
import com.dengjiajia.finance.vo.WishVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishServiceImpl extends ServiceImpl<WishMapper, Wish>
        implements WishService {

    @Override
    public void add(WishDTO dto) {
        Wish wish = new Wish();
        BeanUtils.copyProperties(dto, wish);
        wish.setUserId(UserContext.getUserId());
        wish.setSavedAmount(BigDecimal.ZERO);
        wish.setStatus(1);
        // ✅ 如果没传dailySave，默认10元/天
        if (wish.getDailySave() == null) {
            wish.setDailySave(new BigDecimal("10"));
        }
        this.save(wish);
    }

    @Override
    public void update(WishDTO dto) {
        if (dto.getId() == null) throw new BusinessException("ID不能为空");
        Wish wish = this.getById(dto.getId());
        if (wish == null || !wish.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("愿望不存在");
        }
        BeanUtils.copyProperties(dto, wish);
        this.updateById(wish);
    }

    @Override
    public void delete(Long id) {
        Wish wish = this.getById(id);
        if (wish == null || !wish.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("愿望不存在");
        }
        this.removeById(id);
    }

    @Override
    public List<WishVO> listVO() {
        List<Wish> list = this.lambdaQuery()
                .eq(Wish::getUserId, UserContext.getUserId())
                .orderByAsc(Wish::getStatus)
                .orderByDesc(Wish::getCreateTime)
                .list();
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public void saveAmount(Long id, BigDecimal amount) {
        Wish wish = this.getById(id);
        if (wish == null || !wish.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("愿望不存在");
        }
        BigDecimal newSaved = wish.getSavedAmount().add(amount);
        wish.setSavedAmount(newSaved);
        // 判断是否达成
        if (newSaved.compareTo(wish.getTargetAmount()) >= 0) {
            wish.setStatus(2);
        }
        this.updateById(wish);
    }

    private WishVO toVO(Wish wish) {
        WishVO vo = new WishVO();
        BeanUtils.copyProperties(wish, vo);

        BigDecimal remain = wish.getTargetAmount().subtract(wish.getSavedAmount());
        if (remain.compareTo(BigDecimal.ZERO) < 0) remain = BigDecimal.ZERO;
        vo.setRemainAmount(remain);

        // 完成百分比
        if (wish.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            double pct = wish.getSavedAmount()
                    .divide(wish.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .doubleValue();
            vo.setPercentage(Math.min(pct, 100.0));
        } else {
            vo.setPercentage(0.0);
        }

        // 预计达成天数和日期
        if (wish.getDailySave().compareTo(BigDecimal.ZERO) > 0
                && remain.compareTo(BigDecimal.ZERO) > 0) {
            int days = remain.divide(wish.getDailySave(), 0, RoundingMode.CEILING).intValue();
            vo.setEstimatedDays(days);
            vo.setEstimatedDate(LocalDate.now().plusDays(days));
            vo.setProgressText(String.format("若每天存%.0f元，预计%s达成",
                    wish.getDailySave(), vo.getEstimatedDate()));
        } else if (wish.getStatus() == 2) {
            vo.setProgressText(" 已达成！");
        }

        return vo;
    }
}


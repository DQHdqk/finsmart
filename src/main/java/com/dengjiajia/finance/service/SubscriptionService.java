package com.dengjiajia.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dengjiajia.finance.dto.SubscriptionDTO;
import com.dengjiajia.finance.entity.Subscription;
import com.dengjiajia.finance.vo.SubscriptionVO;
import java.util.List;

public interface SubscriptionService extends IService<Subscription> {
    void add(SubscriptionDTO dto);
    void update(SubscriptionDTO dto);
    void delete(Long id);
    List<SubscriptionVO> listVO();
    List<SubscriptionVO> upcoming(); // 7天内即将扣费
}


package com.dengjiajia.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dengjiajia.finance.dto.WishDTO;
import com.dengjiajia.finance.entity.Wish;
import com.dengjiajia.finance.vo.WishVO;
import java.math.BigDecimal;
import java.util.List;

public interface WishService extends IService<Wish> {
    void add(WishDTO dto);
    void update(WishDTO dto);
    void delete(Long id);
    List<WishVO> listVO();
    void saveAmount(Long id, BigDecimal amount);
}


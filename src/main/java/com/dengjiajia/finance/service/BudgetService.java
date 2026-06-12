package com.dengjiajia.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dengjiajia.finance.dto.BudgetDTO;
import com.dengjiajia.finance.entity.Budget;
import com.dengjiajia.finance.vo.BudgetVO;
import java.util.List;

public interface BudgetService extends IService<Budget> {
    void saveOrUpdate(BudgetDTO dto);
    List<BudgetVO> list(String month);
    void delete(Long id);
}


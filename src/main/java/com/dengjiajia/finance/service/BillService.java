package com.dengjiajia.finance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dengjiajia.finance.dto.BillDTO;
import com.dengjiajia.finance.dto.BillQueryDTO;
import com.dengjiajia.finance.entity.Bill;
import com.dengjiajia.finance.vo.BillVO;

public interface BillService extends IService<Bill> {
    void add(BillDTO dto);
    void update(BillDTO dto);
    void delete(Long id);
    Page<BillVO> page(BillQueryDTO query);
}



package com.dengjiajia.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dengjiajia.finance.dto.MarkPaidDTO;
import com.dengjiajia.finance.dto.ShareBillDTO;
import com.dengjiajia.finance.entity.ShareBill;
import com.dengjiajia.finance.vo.ShareBillVO;
import java.util.List;

public interface ShareService extends IService<ShareBill> {
    void create(ShareBillDTO dto);
    List<ShareBillVO> listVO();
    ShareBillVO detail(Long id);
    void markPaid(MarkPaidDTO dto);
    void deleteBill(Long id);
}


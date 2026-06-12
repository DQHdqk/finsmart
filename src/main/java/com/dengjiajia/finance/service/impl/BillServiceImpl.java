package com.dengjiajia.finance.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dengjiajia.finance.common.BusinessException;
import com.dengjiajia.finance.common.UserContext;
import com.dengjiajia.finance.dto.BillDTO;
import com.dengjiajia.finance.dto.BillQueryDTO;
import com.dengjiajia.finance.entity.Bill;
import com.dengjiajia.finance.entity.Category;
import com.dengjiajia.finance.mapper.BillMapper;
import com.dengjiajia.finance.service.BillService;
import com.dengjiajia.finance.service.CategoryService;
import com.dengjiajia.finance.vo.BillVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillServiceImpl extends ServiceImpl<BillMapper, Bill>
        implements BillService {

    private final CategoryService categoryService;

    @Override
    public void add(BillDTO dto) {
        Bill bill = new Bill();
        BeanUtils.copyProperties(dto, bill);
        bill.setUserId(UserContext.getUserId());
        if (bill.getBillDate() == null) {
            bill.setBillDate(LocalDate.now());
        }
        this.save(bill);
    }

    @Override
    public void update(BillDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("账单ID不能为空");
        }
        Bill bill = this.getById(dto.getId());
        if (bill == null || !bill.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("账单不存在");
        }
        BeanUtils.copyProperties(dto, bill);
        this.updateById(bill);
    }

    @Override
    public void delete(Long id) {
        Bill bill = this.getById(id);
        if (bill == null || !bill.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("账单不存在");
        }
        this.removeById(id);
    }

    @Override
    public Page<BillVO> page(BillQueryDTO query) {
        Long userId = UserContext.getUserId();
        Page<Bill> billPage = this.lambdaQuery()
                .eq(Bill::getUserId, userId)
                .eq(query.getType() != null, Bill::getType, query.getType())
                .eq(query.getCategoryId() != null, Bill::getCategoryId, query.getCategoryId())
                .apply(query.getMonth() != null,
                        "DATE_FORMAT(bill_date,'%Y-%m') = {0}", query.getMonth())
                .orderByDesc(Bill::getBillDate)
                .orderByDesc(Bill::getCreateTime)
                .page(new Page<>(query.getPage(), query.getPageSize()));

        // 转换为VO
        List<BillVO> voList = billPage.getRecords().stream().map(bill -> {
            BillVO vo = new BillVO();
            BeanUtils.copyProperties(bill, vo);
            // 填充分类信息
            Category category = categoryService.getById(bill.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
                vo.setCategoryIcon(category.getIcon());
                vo.setCategoryColor(category.getColor());
            }
            return vo;
        }).collect(Collectors.toList());

        Page<BillVO> voPage = new Page<>(billPage.getCurrent(),
                billPage.getSize(), billPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }
}


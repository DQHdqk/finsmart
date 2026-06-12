package com.dengjiajia.finance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dengjiajia.finance.common.BusinessException;
import com.dengjiajia.finance.common.UserContext;
import com.dengjiajia.finance.dto.MarkPaidDTO;
import com.dengjiajia.finance.dto.ShareBillDTO;
import com.dengjiajia.finance.entity.ShareBill;
import com.dengjiajia.finance.entity.ShareMember;
import com.dengjiajia.finance.mapper.ShareBillMapper;
import com.dengjiajia.finance.mapper.ShareMemberMapper;
import com.dengjiajia.finance.service.ShareService;
import com.dengjiajia.finance.vo.ShareBillVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShareServiceImpl extends ServiceImpl<ShareBillMapper, ShareBill>
        implements ShareService {

    private final ShareMemberMapper shareMemberMapper;

    @Override
    @Transactional
    public void create(ShareBillDTO dto) {
        if (dto.getMemberNames() == null || dto.getMemberNames().isEmpty()) {
            throw new BusinessException("请至少添加一个成员");
        }
        int memberCount = dto.getMemberNames().size();
        BigDecimal perAmount = dto.getTotalAmount()
                .divide(new BigDecimal(memberCount), 2, RoundingMode.HALF_UP);

        ShareBill bill = new ShareBill();
        bill.setUserId(UserContext.getUserId());
        bill.setTitle(dto.getTitle());
        bill.setTotalAmount(dto.getTotalAmount());
        bill.setMemberCount(memberCount);
        bill.setPerAmount(perAmount);
        bill.setStatus(0);
        this.save(bill);

        // 保存成员
        for (String name : dto.getMemberNames()) {
            ShareMember member = new ShareMember();
            member.setShareBillId(bill.getId());
            member.setName(name);
            member.setIsPaid(0);
            shareMemberMapper.insert(member);
        }
    }

    @Override
    public List<ShareBillVO> listVO() {
        List<ShareBill> list = this.lambdaQuery()
                .eq(ShareBill::getUserId, UserContext.getUserId())
                .orderByDesc(ShareBill::getCreateTime)
                .list();
        return list.stream().map(bill -> {
            ShareBillVO vo = new ShareBillVO();
            BeanUtils.copyProperties(bill, vo);
            List<ShareBillVO.MemberVO> members = getMembers(bill.getId(), bill.getPerAmount());
            vo.setMembers(members);
            vo.setPerPersonAmount(bill.getPerAmount());
            vo.setPaidCount((int) members.stream().filter(m -> m.getIsPaid() == 1).count());
            vo.setTotalPaidAmount(members.stream()
                    .map(m -> m.getPaidAmount() != null ? m.getPaidAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            vo.setCreateDate(bill.getCreateTime() != null ? bill.getCreateTime().toString() : "");
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public ShareBillVO detail(Long id) {
        ShareBill bill = this.getById(id);
        if (bill == null || !bill.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("账单不存在");
        }
        ShareBillVO vo = new ShareBillVO();
        BeanUtils.copyProperties(bill, vo);
        List<ShareBillVO.MemberVO> members = getMembers(id, bill.getPerAmount());
        vo.setMembers(members);
        vo.setTotalPaidAmount(members.stream()
                .map(m -> m.getPaidAmount() != null ? m.getPaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return vo;
    }

    @Override
    public void markPaid(MarkPaidDTO dto) {
        ShareMember member = shareMemberMapper.selectById(dto.getMemberId());
        if (member == null) throw new BusinessException("成员不存在");

        ShareBill bill = this.getById(member.getShareBillId());
        if (bill == null) throw new BusinessException("账单不存在");

        BigDecimal paidAmount = dto.getPaidAmount() != null ? dto.getPaidAmount() : BigDecimal.ZERO;
        int isPaid = paidAmount.compareTo(bill.getPerAmount()) >= 0 ? 1 : 0;

        // 用 LambdaUpdateWrapper 显式更新，避免 MyBatis Plus 更新策略忽略 BigDecimal 字段
        shareMemberMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ShareMember>()
                        .eq(ShareMember::getId, dto.getMemberId())
                        .set(ShareMember::getPaidAmount, paidAmount)
                        .set(ShareMember::getIsPaid, isPaid));

        // 所有成员完全付款后才将账单标为已结清
        long unpaidCount = shareMemberMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ShareMember>()
                        .eq(ShareMember::getShareBillId, member.getShareBillId())
                        .eq(ShareMember::getIsPaid, 0));
        if (unpaidCount == 0) {
            ShareBill update = new ShareBill();
            update.setId(member.getShareBillId());
            update.setStatus(1);
            this.updateById(update);
        }
    }

    @Override
    @Transactional
    public void deleteBill(Long id) {
        ShareBill bill = this.getById(id);
        // 校验：账单存在且属于当前用户，防止删别人的
        if (bill == null || !bill.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("账单不存在");
        }
        // 先删掉这个账单下的所有成员
        shareMemberMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ShareMember>()
                        .eq(ShareMember::getShareBillId, id));
        // 再删账单本身
        this.removeById(id);
    }

    private List<ShareBillVO.MemberVO> getMembers(Long billId, BigDecimal perAmount) {
        return shareMemberMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ShareMember>()
                        .eq(ShareMember::getShareBillId, billId)
        ).stream().map(m -> {
            ShareBillVO.MemberVO vo = new ShareBillVO.MemberVO();
            vo.setId(m.getId());
            vo.setName(m.getName());
            vo.setIsPaid(m.getIsPaid());
            vo.setAmount(perAmount);
            vo.setPaidAmount(m.getPaidAmount() != null ? m.getPaidAmount() : BigDecimal.ZERO);
            vo.setPaid(m.getIsPaid() == 1);
            return vo;
        }).collect(Collectors.toList());
    }
}


package com.dengjiajia.finance.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShareBillVO {
    private Long id;
    private String title;
    private BigDecimal totalAmount;
    private BigDecimal perAmount;
    private BigDecimal perPersonAmount; // ✅ 加这个，和perAmount一样的值，给前端用
    private Integer memberCount;
    private Integer paidCount;          // 已付人数
    private BigDecimal totalPaidAmount; // 已收金额合计
    private Integer status;
    private LocalDateTime createTime;
    private String createDate;          // ✅ 加这个，格式化后的日期字符串
    private List<MemberVO> members;

    @Data
    public static class MemberVO {
        private Long id;
        private String name;
        private Integer isPaid;
        private Boolean paid;           // ✅ 加这个，前端用 member.paid 判断
        private BigDecimal amount;
        private BigDecimal paidAmount;
        private LocalDateTime paidTime;
        private String paidRemark;
    }
}


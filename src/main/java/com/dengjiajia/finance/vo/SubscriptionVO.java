package com.dengjiajia.finance.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SubscriptionVO {
    private Long id;
    private String name;
    private BigDecimal amount;
    private String cycle;
    private LocalDate nextDate;
    private String icon;
    private String color;
    private String remark;
    private Integer status;
    private Integer daysUntilNext; // 距离下次扣费天数
    private Boolean isUrgent;      // 7天内为true
    private String cycleText;      // 月付/年付
}


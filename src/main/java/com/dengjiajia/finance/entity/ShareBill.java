package com.dengjiajia.finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("share_bill")
public class ShareBill {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private BigDecimal totalAmount;

    private Integer memberCount;

    private BigDecimal perAmount;

    private Integer status; // 1进行中 2已结清

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}


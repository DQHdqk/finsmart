package com.dengjiajia.finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("share_member")
public class ShareMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shareBillId;

    private String name;

    private Integer isPaid;     // 0未付 1已付

    private BigDecimal paidAmount; // 实际存入金额

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}


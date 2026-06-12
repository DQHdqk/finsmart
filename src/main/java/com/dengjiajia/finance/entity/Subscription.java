package com.dengjiajia.finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("subscription")
public class Subscription {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private BigDecimal amount;

    private String cycle; // monthly/yearly

    private LocalDate nextDate;

    private String icon;

    private String color;

    private String remark;

    private Integer status; // 1启用 0停用

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}


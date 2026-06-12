package com.dengjiajia.finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wish")
public class Wish {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private BigDecimal targetAmount;

    private BigDecimal savedAmount;

    private BigDecimal dailySave;

    private String icon;

    private String color;

    private String remark;

    private Integer status; // 1进行中 2已达成

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}


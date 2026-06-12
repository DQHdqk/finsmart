package com.dengjiajia.finance.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class WishDTO {
    private Long id;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal savedAmount;
    private BigDecimal dailySave;
    private String icon;
    private String color;
    private String remark;
}


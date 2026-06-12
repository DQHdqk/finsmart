package com.dengjiajia.finance.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BillVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private Integer type;
    private BigDecimal amount;
    private String remark;
    private LocalDate billDate;
    private LocalDateTime createTime;
}


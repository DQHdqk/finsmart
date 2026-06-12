package com.dengjiajia.finance.dto;

import lombok.Data;

@Data
public class BillQueryDTO {
    private Integer type;      // 1支出 2收入
    private Long categoryId;   // 分类ID
    private String month;      // 月份 格式2024-01
    private Integer page = 1;
    private Integer pageSize = 10;
}

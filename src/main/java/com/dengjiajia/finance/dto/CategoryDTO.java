package com.dengjiajia.finance.dto;

import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private Integer type; // 1支出 2收入
    private String icon;
    private String color;
    private Integer sort;
}


package com.dengjiajia.finance.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ShareBillDTO {
    private String title;
    private BigDecimal totalAmount;
    private List<String> memberNames; // 成员名字列表
}


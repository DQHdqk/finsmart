package com.dengjiajia.finance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SubscriptionDTO {
    private Long id;
    private String name;
    private BigDecimal amount;
    private String cycle;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextDate;
    private String icon;
    private String color;
    private String remark;
    private Integer status;
}


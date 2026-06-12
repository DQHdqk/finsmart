package com.dengjiajia.finance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.core.JsonParser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.io.IOException;

@Data
@Schema(description = "账单数据传输对象")
public class BillDTO {
    @Schema(description = "账单ID", example = "1")
    private Long id;
    
    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @JsonDeserialize(using = TypeDeserializer.class)
    @Schema(description = "账单类型：1-支出，2-收入", example = "1")
    private Integer type;

    @Schema(description = "金额", example = "100.50")
    private BigDecimal amount;
    
    @Schema(description = "备注", example = "午餐")
    private String remark;
    
    @Schema(description = "账单日期", example = "2024-05-04")
    private LocalDate billDate;

    // 内部类：兼容字符串和数字两种格式
    static class TypeDeserializer extends StdDeserializer<Integer> {
        public TypeDeserializer() { super(Integer.class); }

        @Override
        public Integer deserialize(JsonParser p, DeserializationContext ctx)
                throws IOException {
            String value = p.getText().trim();
            switch (value.toUpperCase()) {
                case "EXPENSE": return 1;
                case "INCOME": return 2;
                case "1": return 1;
                case "2": return 2;
                default:
                    try { return Integer.parseInt(value); }
                    catch (NumberFormatException e) { return 1; }
            }
        }
    }
}


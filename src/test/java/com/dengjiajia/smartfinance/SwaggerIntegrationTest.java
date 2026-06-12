package com.dengjiajia.smartfinance;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SwaggerIntegrationTest {

    @Test
    void contextLoads() {
        // 测试Spring上下文加载成功
        // 这会验证Swagger配置是否正确
    }
}

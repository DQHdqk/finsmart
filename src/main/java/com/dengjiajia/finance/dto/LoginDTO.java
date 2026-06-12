package com.dengjiajia.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
//lombok是代码偷懒神器，帮我自动写重复代码
import lombok.Data;
//@Data自动生成get/set/toString等所有通用方法，让代码超级简洁->lombok注解
@Data
//@Schema->Swagger注解 让别人看懂我的参数是什么意思
@Schema(description = "用户登录数据传输对象")
public class LoginDTO {
    @Schema(description = "用户名", example = "admin")
    private String username;
    
    @Schema(description = "密码", example = "123456")
    private String password;
}


package com.dengjiajia.finance.controller;
//Controller前端（网页 / APP）和后端（Java 代码）沟通的唯一入口
//Controller 只干 3 件事： 1. 接收前端发来的请求（登录？注册？） 2. 转交给 Service 去处理业务 3. 返回结果给前端  它不写复杂逻辑，就是个传递员！
import com.dengjiajia.finance.common.Result;
import com.dengjiajia.finance.dto.LoginDTO;
import com.dengjiajia.finance.dto.RegisterDTO;
import com.dengjiajia.finance.service.UserService;
import com.dengjiajia.finance.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "用户认证", description = "用户注册和登录认证")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册账号")
    public Result<Void> register(@RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录获取JWT Token")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        LoginVO vo = userService.login(dto);
        return Result.success(vo);
    }
}


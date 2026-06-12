package com.dengjiajia.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dengjiajia.finance.common.BusinessException;
import com.dengjiajia.finance.dto.LoginDTO;
import com.dengjiajia.finance.dto.RegisterDTO;
import com.dengjiajia.finance.entity.User;
import com.dengjiajia.finance.mapper.UserMapper;
import com.dengjiajia.finance.service.CategoryService;
import com.dengjiajia.finance.service.UserService;
import com.dengjiajia.finance.utils.JwtUtil;
import com.dengjiajia.finance.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
//告诉SpringBoot这个类是业务处理类，帮我把它放进工厂管理
@Service
//Lombok提供的，自动生成构造方法，不用手写new对象 直接用下面的工具： • jwtUtil（生成登录令牌） • categoryService（初始化分类）
@RequiredArgsConstructor
//③ extends ServiceImpl<UserMapper, User> MyBatis-Plus 提供的超级父类自带现成的增删改查方法，比如： • this.save() → 保存用户 • this.lambdaQuery() → 查询用户不用自己写 SQL！
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;
    private final CategoryService categoryService;
    @Override
    public void register(RegisterDTO dto) {
        // 检查用户名是否已存在
        Long count = this.lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        //创建用户对象，把前端传的数据复制过去
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        // 密码MD5加密
        user.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()));
        //如果没设置昵称 → 默认用用户名当昵称
        if (user.getNickname() == null) {
            user.setNickname(dto.getUsername());
        }
        this.save(user);
        //给新用户创建默认收支分类
        categoryService.initDefault(user.getId());
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = this.lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .one();
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        String md5Password = DigestUtils.md5DigestAsHex(dto.getPassword().getBytes());
        if (!md5Password.equals(user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        // 生成Token
        String token = jwtUtil.generateToken(user.getId());
        //把用户信息 + Token 返回给前端
        LoginVO vo = new LoginVO();
        BeanUtils.copyProperties(user, vo);
        vo.setToken(token);
        return vo;
    }
}


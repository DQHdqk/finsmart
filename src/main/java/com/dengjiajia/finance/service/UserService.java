package com.dengjiajia.finance.service;

import com.dengjiajia.finance.dto.LoginDTO;
import com.dengjiajia.finance.dto.RegisterDTO;
import com.dengjiajia.finance.vo.LoginVO;

public interface UserService {
    void register(RegisterDTO dto);
    LoginVO login(LoginDTO dto);
}


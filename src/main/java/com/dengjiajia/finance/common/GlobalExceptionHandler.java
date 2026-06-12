package com.dengjiajia.finance.common;



import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.bind.annotation.RestControllerAdvice;



@Slf4j

@RestControllerAdvice

public class GlobalExceptionHandler {



    @ExceptionHandler(BusinessException.class)

    public Result<Void> handleBusinessException(BusinessException e) {

        log.error("业务异常: {}", e.getMessage());

        return Result.error(e.getMessage());

    }



    @ExceptionHandler(Exception.class)

    public Result<Void> handleException(Exception e) {

        log.error("系统异常: ", e);  //  改成这样打印完整堆栈

        return Result.error("系统繁忙，请稍后重试");

    }

}






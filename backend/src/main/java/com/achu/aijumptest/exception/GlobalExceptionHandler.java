package com.achu.aijumptest.exception;

import com.achu.aijumptest.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * projectName: com.achu.aijumptest.exceptionhandlers.GlobalExceptionHandler
 *
 * @author: achu_code
 * description: 全局异常处理器
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<Void> exception(Exception e){
        //记录异常日志
        log.error("服务器发生运行时异常！异常信息为:{}",e.getMessage());
        //返回错误信息
        return Result.error(e.getMessage());
    }

    @ExceptionHandler
    public Result<Void> businessException(BusinessException businessException){
        //记录异常日志
        log.warn("发生业务异常:{}",businessException.getMessage());
        //返回错误信息
        return Result.error(businessException.getMessage());
    }
}

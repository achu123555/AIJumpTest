package com.achu.aijumptest.exception;

import com.achu.aijumptest.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
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
        log.error("【服务器发生严重的运行时未知异常！】",e);
        //返回错误信息
        return Result.error("服务器开小差了,请稍后再试~");
    }

    @ExceptionHandler
    public Result<Void> NullPointerException(NullPointerException nullPointerException){
        //记录异常日志
        log.error("【服务器发生空指针异常！】",nullPointerException);
        //返回错误信息
        return Result.error("服务器开小差了,请稍后再试~");
    }

    @ExceptionHandler
    public Result<Void> DuplicateKeyException(DuplicateKeyException duplicateKeyException){
        //记录异常日志
        log.warn("【发生数据库唯一主键冲突异常！】",duplicateKeyException);
        //返回错误信息
        return Result.error("数据已存在,请勿重复提交数据！（例如ID或关键字冲突）");
    }

    @ExceptionHandler
    public Result<Void> businessException(BusinessException businessException){
        // 业务异常保留原始状态码，例如 401 未登录、403 无权限、400 参数错误。
        log.warn("【服务器运行时发生业务异常！】异常码：{}，异常信息为：{}", businessException.getCode(), businessException.getMessage());
        return Result.error(businessException.getCode(), businessException.getMessage());
    }
}

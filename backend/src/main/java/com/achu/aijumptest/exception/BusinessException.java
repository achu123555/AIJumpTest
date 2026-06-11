package com.achu.aijumptest.exception;

import com.achu.aijumptest.enums.ResultCode;
import lombok.Getter;

/**
 * projectName: com.achu.aijumptest.exception.BusinessException
 *
 * @author: achu_code
 * description: 自定义业务异常
 */

@Getter
public class BusinessException extends RuntimeException{

    private final Integer code;

    public BusinessException(String message){
        super(message);
        this.code = ResultCode.INTERNAL_ERROR.getCode();
    }

    public BusinessException(Integer code,String message){
        super(message);
        this.code = code;
    }
}

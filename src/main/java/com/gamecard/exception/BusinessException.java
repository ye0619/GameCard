package com.gamecard.exception;

import com.gamecard.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 *
 * @author GameCard Team
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 异常状态码 */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BUSINESS_ERROR.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResultCode.BUSINESS_ERROR.getCode();
    }

}

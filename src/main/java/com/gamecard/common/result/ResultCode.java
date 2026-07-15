package com.gamecard.common.result;

import lombok.Getter;

/**
 * 通用响应状态码枚举
 *
 * @author GameCard Team
 */
@Getter
public enum ResultCode {

    /** 操作成功 */
    SUCCESS(200, "操作成功"),

    /** 客户端错误 */
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),

    /** 服务端错误 */
    ERROR(500, "服务器内部错误"),

    /** 业务异常 */
    BUSINESS_ERROR(1001, "业务异常"),
    PARAM_ERROR(1002, "参数校验失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}

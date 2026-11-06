package com.group.exam.common;

/**
 * 业务异常：携带可向用户展示的业务提示信息。
 * 由全局异常处理器统一拦截并输出。
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }
}

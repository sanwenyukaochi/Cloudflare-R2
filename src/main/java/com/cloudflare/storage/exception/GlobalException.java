package com.cloudflare.storage.exception;

import cn.hutool.http.HttpStatus;
import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {
    private final Integer code;

    public GlobalException(String message) {
        this.code = HttpStatus.HTTP_INTERNAL_ERROR;
        super(message);
    }

    public GlobalException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public GlobalException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public GlobalException(String message, Throwable cause) {
        this.code = HttpStatus.HTTP_INTERNAL_ERROR;
        super(message, cause);
    }
}

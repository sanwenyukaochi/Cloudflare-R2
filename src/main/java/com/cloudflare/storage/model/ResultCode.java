package com.cloudflare.storage.model;

import cn.hutool.http.HttpStatus;
import lombok.Getter;

/**
 * @author sanwenyukaochi
 * @version 1.0
 * @since 2025-09-28
 */
@Getter
public enum ResultCode {

    SUCCESS(HttpStatus.HTTP_OK, "操作成功"),
    ERROR(HttpStatus.HTTP_INTERNAL_ERROR, "操作失败");
    
    private Integer code;
    private String msg;
    
    ResultCode() {

    }

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}

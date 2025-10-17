package com.cloudflare.storage.model;

import cn.hutool.http.HttpStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

/**
 * @author sanwenyukaochi
 * @version 1.0
 * @since 2025-09-28
 */
@Builder
@JsonInclude
public record Result<T>(Integer code, String msg, T data) {

    public static <T> Result<T> success() {
        return Result.<T>builder().code(HttpStatus.HTTP_OK).msg("操作成功").build();
    }

    public static <T> Result<T> success(T data) {
        return Result.<T>builder().code(HttpStatus.HTTP_OK).msg("操作成功").data(data).build();
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return Result.<T>builder().code(code).msg(msg).build();
    }

    public static <T> Result<T> error(Integer code, String msg, T data) {
        return Result.<T>builder().code(code).msg(msg).data(data).build();
    }

}

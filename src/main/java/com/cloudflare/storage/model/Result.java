package com.cloudflare.storage.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author sanwenyukaochi
 * @version 1.0
 * @since 2025-09-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude
public class Result<T> {

    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success(ResultCode resultCode) {
        return Result.<T>builder()
                .code(resultCode.getCode())
                .msg(resultCode.getMsg())
                .build();
    }

    public static <T> Result<T> success(ResultCode resultCode, T data) {
        return Result.<T>builder()
                .code(resultCode.getCode())
                .msg(resultCode.getMsg())
                .data(data)
                .build();
    }

    public static <T> Result<T> error(ResultCode resultCode, T data) {
        return Result.<T>builder()
                .code(resultCode.getCode())
                .msg(resultCode.getMsg())
                .data(data)
                .build();
    }

}

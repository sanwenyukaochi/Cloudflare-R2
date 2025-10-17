package com.cloudflare.storage.handler;

import cn.hutool.core.text.StrBuilder;
import cn.hutool.http.HttpStatus;
import com.cloudflare.storage.exception.GlobalException;
import com.cloudflare.storage.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
//@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<Result<Object>> handleApiException(GlobalException e) {
        log.warn("业务异常：code={}, msg={}", e.getCode(), e.getMessage());
        return ResponseEntity.status(e.getCode()).body(
                Result.error(e.getCode(), e.getMessage())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleException(Exception e) {
        log.warn("系统异常：code={}, msg={}", HttpStatus.HTTP_INTERNAL_ERROR, e.getMessage());
        return ResponseEntity.status(HttpStatus.HTTP_INTERNAL_ERROR).body(
                Result.error(HttpStatus.HTTP_INTERNAL_ERROR, "服务器繁忙，请稍后再试")
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        StrBuilder strBuilder = new StrBuilder();
        e.getBindingResult().getFieldErrors().forEach(fieldError ->
                strBuilder.append("[").append(fieldError.getField()).append("]")
                        .append(fieldError.getDefaultMessage())
                        .append(";")
        );
        log.error(strBuilder.toString());
        return Result.error(HttpStatus.HTTP_INTERNAL_ERROR, "操作失败");
    }

}

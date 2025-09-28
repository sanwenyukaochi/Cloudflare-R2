package com.cloudflare.storage.handler;

import com.cloudflare.storage.model.Result;
import com.cloudflare.storage.model.ResultCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
//@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public @ResponseBody Result<Object> handException(Exception e) {
        return Result.error(ResultCode.ERROR);
    }
}

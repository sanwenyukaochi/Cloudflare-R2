package com.cloudflare.storage.handler;

import cn.hutool.core.text.StrBuilder;
import com.cloudflare.storage.constant.ErrorPageConstants;
import com.cloudflare.storage.model.Result;
import com.cloudflare.storage.model.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;

@Slf4j
@RestControllerAdvice
//@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public @ResponseBody Result<Object> handException(Exception e) {
        return Result.error(ResultCode.ERROR);
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
        return Result.error(ResultCode.ERROR);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public void handleNotFound(HttpServletResponse response) throws IOException {
        response.sendRedirect(ErrorPageConstants.NOT_FOUND);
    }
}

package com.xyzy.handler;

import com.xyzy.domain.ResponseResult;
import com.xyzy.enums.AppHttpCodeEnum;
import com.xyzy.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(SystemException.class)
    public ResponseResult systemExceptionHandler(SystemException e) {
        log.error("出现了异常: {}", e.getMsg());
        return ResponseResult.errorResult(e.getCode(), e.getMsg());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseResult validationExceptionHandler(Exception e) {
        String message;
        if (e instanceof MethodArgumentNotValidException validation) {
            message = validation.getBindingResult().getFieldErrors().stream()
                    .findFirst().map(error -> error.getDefaultMessage()).orElse(AppHttpCodeEnum.VALIDATION_ERROR.getMsg());
        } else {
            BindException bind = (BindException) e;
            message = bind.getBindingResult().getFieldErrors().stream()
                    .findFirst().map(error -> error.getDefaultMessage()).orElse(AppHttpCodeEnum.VALIDATION_ERROR.getMsg());
        }
        return ResponseResult.errorResult(AppHttpCodeEnum.VALIDATION_ERROR.getCode(), message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseResult malformedRequestHandler(HttpMessageNotReadableException e) {
        return ResponseResult.errorResult(AppHttpCodeEnum.VALIDATION_ERROR.getCode(), "请求体格式错误");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseResult duplicateKeyHandler(DuplicateKeyException e) {
        log.warn("数据库唯一约束冲突", e);
        return ResponseResult.errorResult(AppHttpCodeEnum.DATA_CONFLICT.getCode(), AppHttpCodeEnum.DATA_CONFLICT.getMsg());
    }

    @ExceptionHandler(Exception.class)
    public ResponseResult exceptionHandler(Exception e) {
        log.error("未处理的系统异常", e);
        return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR.getCode(), AppHttpCodeEnum.SYSTEM_ERROR.getMsg());
    }
}

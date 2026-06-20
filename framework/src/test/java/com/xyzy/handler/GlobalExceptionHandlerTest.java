package com.xyzy.handler;

import com.xyzy.domain.ResponseResult;
import com.xyzy.enums.AppHttpCodeEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {

    @Test
    void hidesInternalExceptionDetailsFromClient() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseResult<?> result = handler.exceptionHandler(
                new RuntimeException("SQL password=secret at C:\\private\\path"));

        assertEquals(AppHttpCodeEnum.SYSTEM_ERROR.getCode(), result.getCode());
        assertEquals(AppHttpCodeEnum.SYSTEM_ERROR.getMsg(), result.getMsg());
        assertFalse(result.getMsg().contains("secret"));
    }
}

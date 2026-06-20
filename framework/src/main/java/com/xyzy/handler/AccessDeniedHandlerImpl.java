package com.xyzy.handler;

import com.xyzy.domain.ResponseResult;
import com.xyzy.enums.AppHttpCodeEnum;
import com.xyzy.utils.WebUtils;
import com.alibaba.fastjson.JSON;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ResponseResult result = ResponseResult.errorResult(AppHttpCodeEnum.NO_OPERATOR_AUTH.getCode(), AppHttpCodeEnum.NO_OPERATOR_AUTH.getMsg());
        WebUtils.renderString(response, JSON.toJSONString(result));
    }
}

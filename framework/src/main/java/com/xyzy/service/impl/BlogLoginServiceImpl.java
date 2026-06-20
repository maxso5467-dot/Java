package com.xyzy.service.impl;

import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.entity.LoginUser;
import com.xyzy.domain.entity.User;
import com.xyzy.domain.vo.BlogUserLoginVo;
import com.xyzy.domain.vo.UserInfoVo;
import com.xyzy.enums.AppHttpCodeEnum;
import com.xyzy.exception.SystemException;
import com.xyzy.service.BlogLoginService;
import com.xyzy.service.UserService;
import com.xyzy.utils.BeanCopyUtils;
import com.xyzy.utils.JwtUtil;
import com.xyzy.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class BlogLoginServiceImpl implements BlogLoginService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;

    @Override
    public ResponseResult login(User user) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        if (Objects.isNull(authenticate)) {
            throw new RuntimeException("用户名或密码错误");
        }
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        String userId = loginUser.getUser().getId().toString();
        String jwt = JwtUtil.createJWT(userId);
        redisCache.setCacheObject("bloglogin:" + userId, loginUser);
        UserInfoVo userInfoVo = BeanCopyUtils.copyBean(loginUser.getUser(), UserInfoVo.class);
        BlogUserLoginVo vo = new BlogUserLoginVo(jwt, userInfoVo);
        return ResponseResult.okResult(vo);
    }

    @Override
    public ResponseResult logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();
        redisCache.deleteObject("bloglogin:" + userId);
        return ResponseResult.okResult();
    }
}

package com.xyzy.service;

import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.dto.LoginRequest;
import com.xyzy.domain.entity.LoginUser;
import com.xyzy.domain.vo.AdminLoginVo;
import com.xyzy.domain.vo.UserInfoVo;
import com.xyzy.enums.AppHttpCodeEnum;
import com.xyzy.exception.SystemException;
import com.xyzy.mapper.RoleMapper;
import com.xyzy.security.LoginSessionService;
import com.xyzy.utils.BeanCopyUtils;
import com.xyzy.utils.JwtUtil;
import com.xyzy.utils.SecurityUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminLoginService {
    private final AuthenticationManager authenticationManager;
    private final LoginSessionService sessions;
    private final RoleMapper roleMapper;

    public AdminLoginService(AuthenticationManager authenticationManager, LoginSessionService sessions,
                             RoleMapper roleMapper) {
        this.authenticationManager = authenticationManager;
        this.sessions = sessions;
        this.roleMapper = roleMapper;
    }

    public ResponseResult<AdminLoginVo> login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.userName(), request.password()));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = roleMapper.selectRoleKeysByUserId(loginUser.getUser().getId());
        if (roles == null || !roles.contains("admin")) {
            throw new SystemException(AppHttpCodeEnum.NO_OPERATOR_AUTH);
        }
        sessions.save("admin", loginUser);
        String token = JwtUtil.createJWT(loginUser.getUser().getId().toString());
        UserInfoVo user = BeanCopyUtils.copyBean(loginUser.getUser(), UserInfoVo.class);
        return ResponseResult.okResult(new AdminLoginVo(token, user, roles, loginUser.getPermissions()));
    }

    public ResponseResult<Void> logout() {
        sessions.delete("admin", SecurityUtils.getUserId());
        return ResponseResult.okResult();
    }

    public ResponseResult<AdminLoginVo> getInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        List<String> roles = roleMapper.selectRoleKeysByUserId(loginUser.getUser().getId());
        UserInfoVo user = BeanCopyUtils.copyBean(loginUser.getUser(), UserInfoVo.class);
        return ResponseResult.okResult(new AdminLoginVo(null, user, roles, loginUser.getPermissions()));
    }
}

package com.xyzy.service;

import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.entity.User;

public interface BlogLoginService {
    ResponseResult login(User user);
    ResponseResult logout();
}

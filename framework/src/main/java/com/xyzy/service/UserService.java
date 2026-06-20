package com.xyzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.entity.User;

public interface UserService extends IService<User> {
    ResponseResult userInfo();
    ResponseResult updateUserInfo(User user);
    ResponseResult register(User user);
}

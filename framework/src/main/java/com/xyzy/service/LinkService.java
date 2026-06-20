package com.xyzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.entity.Link;

public interface LinkService extends IService<Link> {
    ResponseResult getAllLink();
}

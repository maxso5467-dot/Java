package com.xyzy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginVo {
    private String token;
    private UserInfoVo user;
    private List<String> roles;
    private List<String> permissions;
}

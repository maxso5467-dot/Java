package com.xyzy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVo {
    private Long id;
    private String userName;
    private String nickName;
    private String email;
    private String phonenumber;
    private String sex;
    private String avatar;
}

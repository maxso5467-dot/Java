package com.xyzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xyzy.domain.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    @Select("""
            SELECT DISTINCT r.role_key
            FROM t_role r
            JOIN t_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId} AND r.status = '0' AND r.del_flag = 0
            """)
    List<String> selectRoleKeysByUserId(Long userId);
}

package com.xyzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xyzy.domain.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<Menu> {
    @Select("""
            SELECT DISTINCT m.perms
            FROM t_menu m
            JOIN t_role_menu rm ON rm.menu_id = m.id
            JOIN t_role r ON r.id = rm.role_id AND r.status = '0' AND r.del_flag = 0
            JOIN t_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId}
              AND m.status = '0' AND m.del_flag = 0
              AND m.perms IS NOT NULL AND m.perms <> ''
            """)
    List<String> selectPermsByUserId(Long userId);
}

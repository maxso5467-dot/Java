package com.xyzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xyzy.domain.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {
    @Select("SELECT menu_id FROM t_role_menu WHERE role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(Long roleId);
}

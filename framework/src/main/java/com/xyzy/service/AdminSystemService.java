package com.xyzy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.dto.AdminUserWriteRequest;
import com.xyzy.domain.dto.MenuWriteRequest;
import com.xyzy.domain.dto.RoleWriteRequest;
import com.xyzy.domain.entity.Menu;
import com.xyzy.domain.entity.Role;
import com.xyzy.domain.entity.RoleMenu;
import com.xyzy.domain.entity.User;
import com.xyzy.domain.entity.UserRole;
import com.xyzy.domain.vo.MenuTreeVo;
import com.xyzy.domain.vo.PageVo;
import com.xyzy.enums.AppHttpCodeEnum;
import com.xyzy.exception.SystemException;
import com.xyzy.mapper.MenuMapper;
import com.xyzy.mapper.RoleMapper;
import com.xyzy.mapper.RoleMenuMapper;
import com.xyzy.mapper.UserMapper;
import com.xyzy.mapper.UserRoleMapper;
import com.xyzy.security.LoginSessionService;
import com.xyzy.utils.BeanCopyUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminSystemService {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final PasswordEncoder passwordEncoder;
    private final LoginSessionService sessions;

    public AdminSystemService(UserMapper userMapper, RoleMapper roleMapper, MenuMapper menuMapper,
                              UserRoleMapper userRoleMapper, RoleMenuMapper roleMenuMapper,
                              PasswordEncoder passwordEncoder, LoginSessionService sessions) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.passwordEncoder = passwordEncoder;
        this.sessions = sessions;
    }

    public ResponseResult<PageVo> userPage(int pageNum, int pageSize, String userName, String status) {
        Page<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<User>()
                .like(StringUtils.hasText(userName), User::getUserName, userName)
                .eq(StringUtils.hasText(status), User::getStatus, status)
                .orderByDesc(User::getCreateTime));
        page.getRecords().forEach(user -> user.setPassword(null));
        return ResponseResult.okResult(new PageVo(page.getRecords(), page.getTotal()));
    }

    @Transactional
    public ResponseResult<Long> createUser(AdminUserWriteRequest request) {
        ensureUserUnique(request, null);
        if (!StringUtils.hasText(request.password())) throw new SystemException(AppHttpCodeEnum.PASSWORD_NOT_NULL);
        User user = toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setType("0");
        userMapper.insert(user);
        replaceUserRoles(user.getId(), request.roleIds());
        return ResponseResult.okResult(user.getId());
    }

    @Transactional
    public ResponseResult<Void> updateUser(Long id, AdminUserWriteRequest request) {
        require(userMapper.selectById(id));
        ensureUserUnique(request, id);
        User user = toUser(request).setId(id);
        if (StringUtils.hasText(request.password())) user.setPassword(passwordEncoder.encode(request.password()));
        userMapper.updateById(user);
        replaceUserRoles(id, request.roleIds());
        sessions.delete("admin", id);
        return ResponseResult.okResult();
    }

    @Transactional
    public ResponseResult<Void> deleteUser(Long id) {
        protectAdministrator(id);
        require(userMapper.selectById(id));
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        userMapper.deleteById(id);
        sessions.delete("admin", id);
        return ResponseResult.okResult();
    }

    public ResponseResult<Void> changeUserStatus(Long id, String status) {
        protectAdministrator(id);
        require(userMapper.selectById(id));
        userMapper.updateById(new User().setId(id).setStatus(status));
        sessions.delete("admin", id);
        return ResponseResult.okResult();
    }

    public ResponseResult<List<Long>> userRoleIds(Long id) {
        require(userMapper.selectById(id));
        return ResponseResult.okResult(userRoleMapper.selectRoleIdsByUserId(id));
    }

    @Transactional
    public ResponseResult<Void> assignUserRoles(Long id, List<Long> roleIds) {
        require(userMapper.selectById(id));
        replaceUserRoles(id, roleIds);
        sessions.delete("admin", id);
        return ResponseResult.okResult();
    }

    public ResponseResult<PageVo> rolePage(int pageNum, int pageSize, String roleName, String status) {
        Page<Role> page = roleMapper.selectPage(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<Role>()
                .like(StringUtils.hasText(roleName), Role::getRoleName, roleName)
                .eq(StringUtils.hasText(status), Role::getStatus, status)
                .orderByAsc(Role::getRoleSort));
        return ResponseResult.okResult(new PageVo(page.getRecords(), page.getTotal()));
    }

    public ResponseResult<List<Role>> roleList() {
        return ResponseResult.okResult(roleMapper.selectList(
                new LambdaQueryWrapper<Role>().eq(Role::getStatus, "0").orderByAsc(Role::getRoleSort)));
    }

    @Transactional
    public ResponseResult<Long> createRole(RoleWriteRequest request) {
        ensureRoleUnique(request, null);
        Role role = toRole(request); roleMapper.insert(role);
        replaceRoleMenus(role.getId(), request.menuIds());
        return ResponseResult.okResult(role.getId());
    }

    @Transactional
    public ResponseResult<Void> updateRole(Long id, RoleWriteRequest request) {
        protectAdministratorRole(id);
        require(roleMapper.selectById(id));
        ensureRoleUnique(request, id);
        Role role = toRole(request); role.setId(id); roleMapper.updateById(role);
        replaceRoleMenus(id, request.menuIds());
        invalidateUsersForRole(id);
        return ResponseResult.okResult();
    }

    @Transactional
    public ResponseResult<Void> deleteRole(Long id) {
        protectAdministratorRole(id);
        require(roleMapper.selectById(id));
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, id));
        roleMapper.deleteById(id);
        return ResponseResult.okResult();
    }

    public ResponseResult<List<Long>> roleMenuIds(Long id) {
        require(roleMapper.selectById(id));
        return ResponseResult.okResult(roleMenuMapper.selectMenuIdsByRoleId(id));
    }

    @Transactional
    public ResponseResult<Void> assignRoleMenus(Long id, List<Long> menuIds) {
        require(roleMapper.selectById(id));
        replaceRoleMenus(id, menuIds);
        invalidateUsersForRole(id);
        return ResponseResult.okResult();
    }

    public ResponseResult<Void> changeRoleStatus(Long id, String status) {
        protectAdministratorRole(id);
        require(roleMapper.selectById(id));
        Role role = new Role(); role.setId(id); role.setStatus(status); roleMapper.updateById(role);
        invalidateUsersForRole(id);
        return ResponseResult.okResult();
    }

    public ResponseResult<List<MenuTreeVo>> menuTree() {
        List<Menu> menus = menuMapper.selectList(new LambdaQueryWrapper<Menu>().orderByAsc(Menu::getOrderNum));
        Map<Long, MenuTreeVo> nodes = menus.stream().map(menu -> BeanCopyUtils.copyBean(menu, MenuTreeVo.class))
                .collect(Collectors.toMap(MenuTreeVo::getId, Function.identity()));
        List<MenuTreeVo> roots = new ArrayList<>();
        for (MenuTreeVo node : nodes.values()) {
            MenuTreeVo parent = nodes.get(node.getParentId());
            if (parent == null || node.getParentId() == 0) roots.add(node); else parent.getChildren().add(node);
        }
        roots.sort(java.util.Comparator.comparing(MenuTreeVo::getOrderNum));
        return ResponseResult.okResult(roots);
    }

    public ResponseResult<Long> createMenu(MenuWriteRequest request) {
        validateMenuParent(null, request.parentId());
        Menu menu = toMenu(request); menuMapper.insert(menu); return ResponseResult.okResult(menu.getId());
    }

    public ResponseResult<Void> updateMenu(Long id, MenuWriteRequest request) {
        require(menuMapper.selectById(id)); validateMenuParent(id, request.parentId());
        Menu menu = toMenu(request); menu.setId(id); menuMapper.updateById(menu); return ResponseResult.okResult();
    }

    @Transactional
    public ResponseResult<Void> deleteMenu(Long id) {
        require(menuMapper.selectById(id));
        if (menuMapper.selectCount(new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, id)) > 0)
            throw new SystemException(AppHttpCodeEnum.DATA_CONFLICT);
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getMenuId, id));
        menuMapper.deleteById(id); return ResponseResult.okResult();
    }

    private void replaceUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (roleIds == null) return;
        new LinkedHashSet<>(roleIds).stream().filter(java.util.Objects::nonNull)
                .forEach(roleId -> userRoleMapper.insert(new UserRole(userId, roleId)));
    }

    private void replaceRoleMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId));
        if (menuIds == null) return;
        new LinkedHashSet<>(menuIds).stream().filter(java.util.Objects::nonNull)
                .forEach(menuId -> roleMenuMapper.insert(new RoleMenu(roleId, menuId)));
    }

    private void invalidateUsersForRole(Long roleId) {
        List<UserRole> links = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, roleId));
        if (links != null) links.forEach(link -> sessions.delete("admin", link.getUserId()));
    }

    private User toUser(AdminUserWriteRequest r) {
        return new User().setUserName(r.userName()).setNickName(r.nickName()).setEmail(r.email())
                .setPhonenumber(r.phonenumber()).setSex(r.sex()).setStatus(defaultString(r.status(), "0"));
    }

    private Role toRole(RoleWriteRequest r) {
        Role role = new Role(); role.setRoleName(r.roleName()); role.setRoleKey(r.roleKey());
        role.setRoleSort(r.roleSort()); role.setStatus(defaultString(r.status(), "0")); return role;
    }

    private Menu toMenu(MenuWriteRequest r) {
        Menu menu = new Menu(); menu.setMenuName(r.menuName()); menu.setParentId(r.parentId());
        menu.setOrderNum(r.orderNum()); menu.setPath(r.path()); menu.setComponent(r.component());
        menu.setMenuType(r.menuType()); menu.setVisible(defaultString(r.visible(), "0"));
        menu.setStatus(defaultString(r.status(), "0")); menu.setPerms(r.perms()); menu.setIcon(r.icon()); return menu;
    }

    private void ensureUserUnique(AdminUserWriteRequest r, Long excludedId) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUserName, r.userName())
                .ne(excludedId != null, User::getId, excludedId)) > 0) throw new SystemException(AppHttpCodeEnum.USERNAME_EXIST);
        if (StringUtils.hasText(r.email()) && userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, r.email())
                .ne(excludedId != null, User::getId, excludedId)) > 0) throw new SystemException(AppHttpCodeEnum.EMAIL_EXIST);
    }

    private void ensureRoleUnique(RoleWriteRequest r, Long excludedId) {
        if (roleMapper.selectCount(new LambdaQueryWrapper<Role>().eq(Role::getRoleKey, r.roleKey())
                .ne(excludedId != null, Role::getId, excludedId)) > 0) throw new SystemException(AppHttpCodeEnum.DATA_CONFLICT);
    }

    private void validateMenuParent(Long id, Long parentId) {
        if (id != null && id.equals(parentId)) throw new SystemException(AppHttpCodeEnum.DATA_CONFLICT);
        if (parentId != 0) require(menuMapper.selectById(parentId));
    }

    private void protectAdministrator(Long id) { if (Long.valueOf(1L).equals(id)) throw new SystemException(AppHttpCodeEnum.DATA_CONFLICT); }
    private void protectAdministratorRole(Long id) { if (Long.valueOf(1L).equals(id)) throw new SystemException(AppHttpCodeEnum.DATA_CONFLICT); }
    private <T> T require(T value) { if (value == null) throw new SystemException(AppHttpCodeEnum.DATA_NOT_FOUND); return value; }
    private String defaultString(String value, String fallback) { return StringUtils.hasText(value) ? value : fallback; }
}

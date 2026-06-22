# XyzyBlog — 博客系统后端项目

> Spring Boot 综合实训项目 | Java 17 + Spring Boot 3 + MyBatis-Plus + Spring Security + JWT + Redis + AOP

---

## 一、项目概述

本项目是一个完整的博客系统后端，采用标准的三层架构：**Controller → Service → Mapper**。

Maven 多模块结构：

```
XyzyBlog
├── framework    公共模块（Entity / VO / DTO / Mapper / Service / 工具类）
├── blog         前台模块（Controller / Security / JWT / AOP / 定时任务）
└── admin        后台模块（管理员 CRUD + RBAC 权限控制）
```

技术栈对照：

| 技术 | 用途 | 用到的地方 |
|------|------|------------|
| Spring Boot 3.3.5 | 项目框架 | `BlogApplication` / `AdminApplication` |
| MyBatis-Plus 3.5.7 | ORM | 所有 Mapper 继承 `BaseMapper`，Service 继承 `ServiceImpl` |
| MySQL | 数据存储 | 11 张表，`sql/init.sql` |
| Redis | 缓存 | 存登录用户 `bloglogin:userId`、存浏览量 `article:viewCount` |
| Spring Security | 认证授权 | `SecurityConfig`、`UserDetailsServiceImpl` |
| JWT | Token | `JwtUtil` 生成/解析，`JwtAuthenticationTokenFilter` 拦截 |
| AOP | 日志 | `LogAspect` + `@SystemLog` 注解 |
| 定时任务 | 同步 | `UpdateViewCountJob` 每 5 分钟 Redis → MySQL |
| FastJson | 序列化 | `FastJsonRedisSerializer` 序列化 Redis 中的对象 |
| BCrypt | 加密 | `BCryptPasswordEncoder` 加密用户密码 |

---

## 二、环境与启动

| 软件 | 版本 | 端口 |
|------|------|------|
| JDK | 17+ | — |
| Maven | 3.6+ | — |
| MySQL | 5.7+ / 8.0+ | 3306 |
| Redis | 5.0+ / 7.0+ | 6379 |

### 初始化

```sql
-- 在 MySQL 中执行
source sql/init.sql;
```

管理员账号：`admin` / `admin123`

### 启动

运行 `blog/src/main/java/com/xyzy/BlogApplication.java`，端口 **7777**。

启动类关键注解：

```java
@SpringBootApplication
@MapperScan("com.xyzy.mapper")     // 扫描 Mapper
@EnableScheduling                   // 开启定时任务
public class BlogApplication { ... }
```

### 配置文件

`blog/src/main/resources/application.yml`：

```yaml
server:
  port: 7777

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/myblog?characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456

mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: delFlag     # 逻辑删除字段
      logic-delete-value: 1
      logic-not-delete-value: 0
      id-type: auto                    # 主键自增
```

---

## 三、公共基础代码

在写业务之前，先建好了以下公共组件（位于 `framework` 模块）。

### 3.1 统一返回类 `ResponseResult`

**文件**：`framework/.../domain/ResponseResult.java`

```java
public class ResponseResult<T> {
    private Integer code;
    private String msg;
    private T data;

    public static ResponseResult okResult() { ... }                    // {code:200, msg:"操作成功"}
    public static ResponseResult okResult(T data) { ... }              // {code:200, msg:"操作成功", data: ...}
    public static ResponseResult errorResult(int code, String msg) { ... }  // {code:xxx, msg:"..."}
}
```

**所有接口统一返回格式**：
```json
{ "code": 200, "msg": "操作成功", "data": {} }
```

### 3.2 状态码枚举 `AppHttpCodeEnum`

**文件**：`framework/.../enums/AppHttpCodeEnum.java`

```java
public enum AppHttpCodeEnum {
    SUCCESS(200, "操作成功"),
    NEED_LOGIN(401, "需要登录后操作"),
    NO_OPERATOR_AUTH(403, "无权限操作"),
    SYSTEM_ERROR(500, "出现错误"),
    LOGIN_ERROR(505, "用户名或密码错误"),
    USERNAME_EXIST(501, "用户名已存在"),
    EMAIL_EXIST(503, "邮箱已存在"),
    USERNAME_NOT_NULL(508, "用户名不能为空"),
    ...
}
```

### 3.3 工具类

| 类 | 文件 | 作用 |
|----|------|------|
| `BeanCopyUtils` | `utils/BeanCopyUtils.java` | `copyBean()` / `copyBeanList()` — 用反射把 Entity 转 VO |
| `JwtUtil` | `utils/JwtUtil.java` | `createJWT()` 生成 Token / `parseJWT()` 解析 Token，HS256 签名 |
| `RedisCache` | `utils/RedisCache.java` | 封装 `RedisTemplate`，提供 String/Hash/List/Set 操作 |
| `SecurityUtils` | `utils/SecurityUtils.java` | 从 `SecurityContextHolder` 中拿当前登录的 `userId` |

### 3.4 系统常量 `SystemConstants`

**文件**：`framework/.../constants/SystemConstants.java`

```java
public class SystemConstants {
    public static final String ARTICLE_STATUS_NORMAL = "0";       // 文章已发布
    public static final String CATEGORY_STATUS_NORMAL = "0";      // 分类正常
    public static final String LINK_STATUS_NORMAL = "0";          // 友链已审核
    public static final String COMMENT_TYPE_ARTICLE = "0";        // 文章评论
    public static final String REDIS_ARTICLE_VIEWCOUNT = "article:viewCount";  // Redis key
    public static final String REDIS_LOGIN_USER_PREFIX = "bloglogin:";         // Redis 前缀
}
```

### 3.5 MyBatis-Plus 自动填充 `MyMetaObjectHandler`

**文件**：`framework/.../handler/MyMetaObjectHandler.java`

```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        // 自动填 createTime、createBy、updateTime、updateBy
    }
    @Override
    public void updateFill(MetaObject metaObject) {
        // 自动填 updateTime、updateBy
    }
}
```

这样每个 Entity 的 `createTime`、`updateTime` 等字段不需要手动 set，MyBatis-Plus 自动填充。

---

## 四、前台接口 — Controller → Service → Mapper 完整调用链

### 4.1 热门文章 `GET /article/hotArticleList`

**调用链**：

```
浏览器请求
  → ArticleController.hotArticleList()
    → ArticleServiceImpl.hotArticleList()
      → ArticleMapper（继承 BaseMapper<Article>）
        → 查 t_article 表
      ← List<Article>
    ← BeanCopyUtils.copyBeanList(articles, HotArticleVo.class)
  ← ResponseResult.okResult(vos)
→ 浏览器收到 JSON
```

**逐层代码**：

`blog/.../controller/ArticleController.java:17-19` — Controller 只做路由，不写逻辑：
```java
@GetMapping("/hotArticleList")
public ResponseResult hotArticleList() {
    return articleService.hotArticleList();
}
```

`framework/.../service/impl/ArticleServiceImpl.java:36-44` — Service 写业务逻辑：
```java
public ResponseResult hotArticleList() {
    // 构建查询条件
    LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Article::getStatus, SystemConstants.ARTICLE_STATUS_NORMAL);  // status = '0'
    queryWrapper.orderByDesc(Article::getViewCount);                              // 按浏览量降序
    Page<Article> page = new Page<>(1, 10);                                       // 只取前 10 条
    page(page, queryWrapper);                                                     // 执行分页查询

    // Entity → VO
    List<HotArticleVo> vos = BeanCopyUtils.copyBeanList(page.getRecords(), HotArticleVo.class);
    return ResponseResult.okResult(vos);
}
```

`framework/.../mapper/ArticleMapper.java` — Mapper 只需继承 BaseMapper，零 SQL：
```java
public interface ArticleMapper extends BaseMapper<Article> {
}
```

`framework/.../domain/entity/Article.java` — Entity 映射 `t_article` 表：
```java
@TableName("t_article")
public class Article {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private Long categoryId;
    private Long viewCount;
    private String status;    // '0'已发布 '1'草稿
    private String isTop;     // '0'普通 '1'置顶
    // ...
}
```

`framework/.../domain/vo/HotArticleVo.java` — VO 只返回前端需要的字段：
```java
public class HotArticleVo {
    private Long id;
    private String title;
    private Long viewCount;
}
```

---

### 4.2 文章列表分页 `GET /article/articleList?pageNum=1&pageSize=10&categoryId=2`

**调用链**：

```
ArticleController.articleList(pageNum, pageSize, categoryId)
  → ArticleServiceImpl.articleList(pageNum, pageSize, categoryId)
    → LambdaQueryWrapper 构建条件:
        status = '0'（已发布）
        categoryId = ?（如果传了）
        orderByDesc(isTop)（置顶排前面）
    → MyBatis-Plus 分页插件自动生成 LIMIT 子句
    → 查 t_article
    → Redis 中取最新浏览量覆盖
    → for 循环查 t_category 补分类名
    → Entity → ArticleListVo → PageVo(rows, total)
  ← ResponseResult.okResult(pageVo)
```

**关键代码** — `ArticleServiceImpl.java:48-72`：
```java
public ResponseResult articleList(Integer pageNum, Integer pageSize, Long categoryId) {
    LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Article::getStatus, SystemConstants.ARTICLE_STATUS_NORMAL);
    queryWrapper.eq(Objects.nonNull(categoryId) && categoryId > 0,
                    Article::getCategoryId, categoryId);              // 条件动态拼接
    queryWrapper.orderByDesc(Article::getIsTop);                      // 置顶排前面

    Page<Article> page = new Page<>(pageNum, pageSize);
    page(page, queryWrapper);

    // 从 Redis 取最新浏览量覆盖
    List<Article> articles = page.getRecords().stream().map(article -> {
        Integer viewCount = redisCache.getCacheMapValue(
            SystemConstants.REDIS_ARTICLE_VIEWCOUNT, article.getId().toString());
        if (viewCount != null) article.setViewCount(viewCount.longValue());
        return article;
    }).collect(Collectors.toList());

    // Entity → VO，并且补分类名
    List<ArticleListVo> articleListVos = BeanCopyUtils.copyBeanList(articles, ArticleListVo.class);
    for (ArticleListVo vo : articleListVos) {
        Category category = categoryService.getById(vo.getCategoryId());
        if (category != null) vo.setCategoryName(category.getName());
    }

    PageVo pageVo = new PageVo(articleListVos, page.getTotal());
    return ResponseResult.okResult(pageVo);
}
```

**MyBatis-Plus 分页插件必须注册** — `blog/.../config/MyBatisPlusConfig.java`：
```java
@Configuration
public class MyBatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());  // ← 分页插件
        return interceptor;
    }
}
```

`PageVo` 结构：
```java
public class PageVo {
    private List rows;     // 当前页数据
    private Long total;    // 总条数
}
```

`ArticleListVo` 相比 `HotArticleVo` 多了 `summary`、`categoryName`、`thumbnail`、`createTime`。

---

### 4.3 文章详情 `GET /article/{id}`

**调用链**：

```
ArticleController.getArticleDetail(@PathVariable Long id)
  → ArticleServiceImpl.getArticleDetail(id)
    → this.getById(id)              // MyBatis-Plus 内置方法，查 t_article
    → Redis 补浏览量
    → categoryService.getById()      // 查 t_category 补分类名
    → BeanCopyUtils.copyBean(article, ArticleDetailVo.class)
  ← ResponseResult.okResult(articleDetailVo)
```

**关键代码** — `ArticleServiceImpl.java:75-88`：
```java
public ResponseResult getArticleDetail(Long id) {
    Article article = getById(id);                           // BaseMapper 自带

    // Redis 浏览量
    Integer viewCount = redisCache.getCacheMapValue(
        SystemConstants.REDIS_ARTICLE_VIEWCOUNT, id.toString());
    if (viewCount != null) article.setViewCount(viewCount.longValue());

    ArticleDetailVo vo = BeanCopyUtils.copyBean(article, ArticleDetailVo.class);

    Category category = categoryService.getById(vo.getCategoryId());
    if (category != null) vo.setCategoryName(category.getName());

    return ResponseResult.okResult(vo);
}
```

`ArticleDetailVo` 包含 `title`、`content`（正文）、`categoryName`、`viewCount`、`isComment`、`createTime`。

---

### 4.4 浏览量为 `PUT /article/updateViewCount/{id}`

不直接写 MySQL，先写 Redis，定时任务统一同步：

```
ArticleController.updateViewCount(@PathVariable Long id)
  → ArticleServiceImpl.updateViewCount(id)
    → redisCache.incrementCacheMapValue("article:viewCount", "id", 1)
      → Redis 执行 HINCRBY article:viewCount 1 1
```

**代码** — `ArticleServiceImpl.java:91-93`：
```java
public ResponseResult updateViewCount(Long id) {
    redisCache.incrementCacheMapValue(
        SystemConstants.REDIS_ARTICLE_VIEWCOUNT, id.toString(), 1);
    return ResponseResult.okResult();
}
```

---

### 4.5 分类列表 `GET /category/getCategoryList`

**调用链**：

```
CategoryController.getCategoryList()
  → CategoryServiceImpl.getCategoryList()
    → 查 t_category WHERE status = '0'
    → Entity → CategoryVo（只取 id + name）
  ← ResponseResult.okResult(categoryVos)
```

**代码** — `CategoryServiceImpl.java:20-26`：
```java
public ResponseResult getCategoryList() {
    LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Category::getStatus, SystemConstants.CATEGORY_STATUS_NORMAL);
    List<Category> categories = list(queryWrapper);
    List<CategoryVo> categoryVos = BeanCopyUtils.copyBeanList(categories, CategoryVo.class);
    return ResponseResult.okResult(categoryVos);
}
```

---

### 4.6 友情链接 `GET /link/getAllLink`

**调用链**：

```
LinkController.getAllLink()
  → LinkServiceImpl.getAllLink()
    → 查 t_link WHERE status = '0'
    → Entity → LinkVo
  ← ResponseResult.okResult(linkVos)
```

**代码** — `LinkServiceImpl.java:20-26`：
```java
public ResponseResult getAllLink() {
    LambdaQueryWrapper<Link> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Link::getStatus, SystemConstants.LINK_STATUS_NORMAL);
    List<Link> links = list(queryWrapper);
    List<LinkVo> linkVos = BeanCopyUtils.copyBeanList(links, LinkVo.class);
    return ResponseResult.okResult(linkVos);
}
```

---

### 4.7 文章评论列表 `GET /comment/commentList?articleId=1`

**调用链**：

```
CommentController.commentList(articleId, pageNum, pageSize)
  → CommentServiceImpl.commentList("0", articleId, pageNum, pageSize)
    → 查 t_comment WHERE type='0' AND articleId=? AND rootId=-1  ← 只查一级评论
    → 分页
    → toCommentVoList() 组装树形结构:
        ┌ 遍历每条一级评论，查用户名、目标用户名
        ├ 过滤出根评论 (rootId == -1)
        └ 每条根评论调用 getChildren() 找子评论
    → 返回 PageVo(一级评论列表, total)
```

**核心逻辑** — 树形组装 `CommentServiceImpl.java:57-78`：
```java
private List<CommentVo> toCommentVoList(List<Comment> list) {
    List<CommentVo> commentVos = BeanCopyUtils.copyBeanList(list, CommentVo.class);

    // 补用户名
    for (CommentVo vo : commentVos) {
        User user = userService.getById(vo.getCreateBy());
        if (user != null) vo.setUsername(user.getNickName());
        // 补被回复者的用户名
        if (vo.getToCommentUserId() != null && vo.getToCommentUserId() != -1) {
            User toUser = userService.getById(vo.getToCommentUserId());
            if (toUser != null) vo.setToCommentUserName(toUser.getNickName());
        }
    }

    // 只保留一级评论
    commentVos = commentVos.stream()
            .filter(o -> o.getRootId() == null || o.getRootId() == -1)
            .collect(Collectors.toList());

    // 每条一级评论找子评论
    for (CommentVo vo : commentVos) {
        vo.setChildren(getChildren(vo.getId(), list));
    }
    return commentVos;
}

// 递归找子评论
private List<CommentVo> getChildren(Long id, List<Comment> list) {
    return list.stream()
            .filter(o -> Objects.equals(o.getRootId(), id))  // rootId 等于父评论 id
            .map(o -> { ... 补用户名 ... })
            .collect(Collectors.toList());
}
```

`CommentVo` 的 `children` 字段使得前端能渲染嵌套评论：
```java
public class CommentVo {
    private Long id;
    private String content;
    private String username;           // 评论者昵称
    private String toCommentUserName;  // 被回复者昵称
    private List<CommentVo> children;  // 子评论列表 ← 树形结构
    // ...
}
```

---

### 4.8 发表评论 `POST /comment`（需要登录）

**调用链**：

```
CommentController.addComment(@RequestBody AddCommentDto)
  → CommentServiceImpl.addComment(addCommentDto)
    → 校验 content 不为空
    → SecurityUtils.getUserId() ← 拿当前登录用户 ID
    → DTO → Comment Entity（createBy 自动填入用户 ID）
    → this.save(comment) ← MyBatis-Plus 内置方法
  ← ResponseResult.okResult()
```

**代码** — `CommentServiceImpl.java:47-54`：
```java
public ResponseResult addComment(AddCommentDto dto) {
    if (!StringUtils.hasText(dto.getContent())) {
        throw new SystemException(AppHttpCodeEnum.CONTENT_NOT_NULL);
    }
    Comment comment = BeanCopyUtils.copyBean(dto, Comment.class);
    comment.setCreateBy(SecurityUtils.getUserId());  // 从 SecurityContext 拿当前用户
    save(comment);
    return ResponseResult.okResult();
}
```

`AddCommentDto` 是前端传来的 JSON 结构：
```java
public class AddCommentDto {
    private Long articleId;       // 文章 id
    private String type;          // "0" 文章评论 "1" 友链评论
    private Long rootId;          // -1 一级评论，其他值=回复根评论
    private String content;       // 评论内容
    private Long toCommentUserId; // 回复谁
    private Long toCommentId;     // 回复哪条评论
}
```

---

### 4.9 用户注册 `POST /user/register`

**调用链**：

```
UserController.register(@RequestBody User)
  → UserServiceImpl.register(user)
    → 逐项校验：userName/password/nickName/email 不为空
    → 逐项校验：userName/nickName/email 不重复
    → BCryptPasswordEncoder 加密密码
    → this.save(user)
  ← ResponseResult.okResult()
```

**代码** — `UserServiceImpl.java:43-67`：
```java
public ResponseResult register(User user) {
    // ===== 不能为空 =====
    if (!StringUtils.hasText(user.getUserName()))
        throw new SystemException(AppHttpCodeEnum.USERNAME_NOT_NULL);   // 508
    if (!StringUtils.hasText(user.getPassword()))
        throw new SystemException(AppHttpCodeEnum.PASSWORD_NOT_NULL);   // 510
    if (!StringUtils.hasText(user.getNickName()))
        throw new SystemException(AppHttpCodeEnum.NICKNAME_NOT_NULL);   // 509
    if (!StringUtils.hasText(user.getEmail()))
        throw new SystemException(AppHttpCodeEnum.EMAIL_NOT_NULL);      // 511

    // ===== 不能重复 =====
    if (userNameExist(user.getUserName()))
        throw new SystemException(AppHttpCodeEnum.USERNAME_EXIST);      // 501
    if (nickNameExist(user.getNickName()))
        throw new SystemException(AppHttpCodeEnum.NICKNAME_EXIST);      // 512
    if (emailExist(user.getEmail()))
        throw new SystemException(AppHttpCodeEnum.EMAIL_EXIST);         // 503

    // 密码加密
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    save(user);
    return ResponseResult.okResult();
}

// 查数据库判断是否重复
private boolean userNameExist(String userName) {
    LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
    qw.eq(User::getUserName, userName);
    return count(qw) > 0;
}
```

---

## 五、登录认证 — 完整流程详解

这是项目中最复杂的部分，涉及 10 个类协作。

### 5.1 整体流程图

```
┌─ 前端 POST /login {userName, password} ────────────────────────────────────┐
│                                                                             │
│  1. BlogLoginController.login(user)                                        │
│       ↓                                                                     │
│  2. BlogLoginServiceImpl.login(user)                                       │
│       ↓                                                                     │
│  3. AuthenticationManager.authenticate(token)                              │
│       ↓  Spring Security 自动调用                                             │
│  4. UserDetailsServiceImpl.loadUserByUsername("admin")                     │
│       ↓                                                                     │
│  5. UserMapper 查 t_user WHERE user_name = 'admin'                         │
│       ↓                                                                     │
│  6. MenuMapper.selectPermsByUserId(1) → 权限列表                            │
│       ↓                                                                     │
│  7. new LoginUser(user, permissions) ← 实现 UserDetails                     │
│       ↓                                                                     │
│  8. 返回 Authentication 对象（含 LoginUser）                                  │
│       ↓                                                                     │
│  9. JwtUtil.createJWT(userId) → 生成 JWT 字符串                             │
│       ↓                                                                     │
│ 10. redisCache.setCacheObject("bloglogin:1", loginUser) → 存 Redis          │
│       ↓                                                                     │
│ 11. 组装 BlogUserLoginVo {token, userInfo}                                 │
│       ↓                                                                     │
│ 12. ResponseResult.okResult(vo) → 返回给前端                                 │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 5.2 逐层代码

**`BlogLoginController`** — 接收请求参数，调用 Service：
```java
@RestController
public class BlogLoginController {
    @Autowired
    private BlogLoginService blogLoginService;

    @PostMapping("/login")
    public ResponseResult login(@RequestBody User user) {       // 前端传 {userName, password}
        return blogLoginService.login(user);
    }
}
```

**`BlogLoginServiceImpl`** — 核心登录逻辑：
```java
@Service
public class BlogLoginServiceImpl implements BlogLoginService {
    @Autowired
    private AuthenticationManager authenticationManager;       // Spring Security 注入
    @Autowired
    private RedisCache redisCache;

    public ResponseResult login(User user) {
        // 1. 封装认证令牌
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword());

        // 2. 交给 Spring Security 认证 → 内部调用 UserDetailsServiceImpl
        Authentication authenticate = authenticationManager.authenticate(authToken);

        // 3. 拿出认证后的用户信息
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        String userId = loginUser.getUser().getId().toString();

        // 4. 生成 JWT
        String jwt = JwtUtil.createJWT(userId);               // userId 作为 subject

        // 5. 用户信息存 Redis（key = bloglogin:1）
        redisCache.setCacheObject("bloglogin:" + userId, loginUser);

        // 6. User → UserInfoVo（脱敏，不返回密码）
        UserInfoVo userInfoVo = BeanCopyUtils.copyBean(loginUser.getUser(), UserInfoVo.class);

        // 7. 返回 token + 用户信息
        return ResponseResult.okResult(new BlogUserLoginVo(jwt, userInfoVo));
    }
}
```

**`UserDetailsServiceImpl`** — Spring Security 会调这个类查数据库：
```java
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) {
        // 1. 查用户
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getUserName, username));

        // 2. 校验：用户不存在 / 被停用 / 已删除 → 抛异常
        if (user == null || !"0".equals(user.getStatus()) || user.getDelFlag() == 1) {
            throw new SystemException(AppHttpCodeEnum.LOGIN_ERROR);  // 505
        }

        // 3. 查该用户的权限列表（五表联查）
        List<String> permissions = menuMapper.selectPermsByUserId(user.getId());

        // 4. 返回 Spring Security 需要的 UserDetails
        return new LoginUser(user, permissions);
    }
}
```

**`MenuMapper.selectPermsByUserId`** — 五表联查拿到权限标识：
```java
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
```

**`LoginUser`** — 实现 `UserDetails` 接口，包装 User + 权限：
```java
public class LoginUser implements UserDetails {
    private User user;                    // 用户实体
    private List<String> permissions;     // 权限字符串列表

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 把 permissions 转成 SimpleGrantedAuthority 集合
        return permissions.stream()
            .filter(p -> p != null && !p.isBlank())
            .map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public String getPassword() { return user.getPassword(); }
    @Override
    public String getUsername() { return user.getUserName(); }
    // isAccountNonExpired / isAccountNonLocked / isCredentialsNonExpired → 全部 true
}
```

**`JwtUtil`** — 生成和解析 JWT：
```java
public class JwtUtil {
    public static final long JWT_TTL = 24 * 60 * 60 * 1000L;  // 有效期 24 小时
    public static final String JWT_KEY = "xyzyblog";

    // 生成 Token：subject = userId
    public static String createJWT(String subject) {
        JwtBuilder builder = Jwts.builder()
            .setId(UUID.randomUUID().toString())
            .setSubject(subject)
            .setIssuer("xyzy")
            .setIssuedAt(new Date())
            .signWith(SignatureAlgorithm.HS256, generalKey())
            .setExpiration(new Date(System.currentTimeMillis() + JWT_TTL));
        return builder.compact();
    }

    // 解析 Token
    public static Claims parseJWT(String jwt) throws Exception {
        return Jwts.parser().setSigningKey(generalKey()).parseClaimsJws(jwt).getBody();
    }
}
```

---

### 5.3 JWT 过滤器 — 请求拦截

**文件**：`blog/.../filter/JwtAuthenticationTokenFilter.java`

每个请求都会经过这个过滤器：

```
请求到达
  → 取 Header 中的 "token"
  → 如果没 token → 直接放行（后续 SecurityConfig 判断是否需要登录）
  → 如果有 token:
      → JwtUtil.parseJWT(token) 解析出 userId
      → redisCache.getCacheObject("bloglogin:" + userId) 查 Redis
      → 如果 Redis 没有 → 直接放行（登录过期）
      → 如果 Redis 有 → 把 LoginUser 存入 SecurityContextHolder
  → 放行
```

**代码**：
```java
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    private RedisCache redisCache;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) {
        // 1. 获取 token
        String token = request.getHeader("token");
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);  // 没 token，放行
            return;
        }

        // 2. 解析 token 拿 userId
        Claims claims = JwtUtil.parseJWT(token);
        String userId = claims.getSubject();

        // 3. 从 Redis 取 LoginUser
        LoginUser loginUser = redisCache.getCacheObject("bloglogin:" + userId);
        if (loginUser == null) {
            filterChain.doFilter(request, response);  // 过期了，放行
            return;
        }

        // 4. 存入 SecurityContext，标记为已登录
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
```

---

### 5.4 Security 配置 — 哪些接口需要登录

**文件**：`blog/.../config/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())                                   // 关闭 CSRF
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // 无状态
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login").anonymous()                      // 登录：匿名可访问
                .requestMatchers("/logout").authenticated()                 // 退出：需要登录
                .requestMatchers("/comment").authenticated()                // 发表评论：需要登录
                .requestMatchers("/user/userInfo").authenticated()          // 个人信息：需要登录
                .anyRequest().permitAll()                                   // 其他全部放行
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)         // 未登录处理
                .accessDeniedHandler(accessDeniedHandler)                   // 无权限处理
            )
            // JWT 过滤器加在 UsernamePasswordAuthenticationFilter 之前
            .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

---

### 5.5 退出登录 `POST /logout`

```java
public ResponseResult logout() {
    // 从 SecurityContext 拿当前用户
    LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext()
                                .getAuthentication().getPrincipal();
    Long userId = loginUser.getUser().getId();
    // 删除 Redis 中的登录信息
    redisCache.deleteObject("bloglogin:" + userId);
    return ResponseResult.okResult();
}
```

---

### 5.6 个人信息 `GET/PUT /user/userInfo`

**查看** — `UserServiceImpl.userInfo()`：
```java
public ResponseResult userInfo() {
    Long userId = SecurityUtils.getUserId();         // 从 SecurityContext 拿当前 userId
    User user = getById(userId);                     // MyBatis-Plus 内置方法
    UserInfoVo vo = BeanCopyUtils.copyBean(user, UserInfoVo.class);  // 脱敏，不返回密码
    return ResponseResult.okResult(vo);
}
```

`UserInfoVo` 不含 `password` 字段：
```java
public class UserInfoVo {
    private Long id;
    private String userName;
    private String nickName;
    private String email;
    private String sex;
    private String avatar;
}
```

---

## 六、统一异常处理

**文件**：`framework/.../handler/GlobalExceptionHandler.java`

用 `@RestControllerAdvice` 拦截所有异常，统一返回 `ResponseResult`：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常（自己抛的 SystemException）
    @ExceptionHandler(SystemException.class)
    public ResponseResult systemExceptionHandler(SystemException e) {
        return ResponseResult.errorResult(e.getCode(), e.getMsg());
    }

    // 参数校验失败
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseResult validationExceptionHandler(Exception e) { ... }

    // 数据库唯一约束冲突
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseResult duplicateKeyHandler(DuplicateKeyException e) { ... }

    // 兜底：未捕获的其他异常
    @ExceptionHandler(Exception.class)
    public ResponseResult exceptionHandler(Exception e) {
        return ResponseResult.errorResult(500, "出现错误");
    }
}
```

**未登录处理** — `AuthenticationEntryPointImpl`：
```java
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {
        ResponseResult result = ResponseResult.errorResult(401, "需要登录后操作");
        WebUtils.renderString(response, JSON.toJSONString(result));   // 写回 JSON
    }
}
```

**无权限处理** — `AccessDeniedHandlerImpl`：
```java
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException e) {
        ResponseResult result = ResponseResult.errorResult(403, "无权限操作");
        WebUtils.renderString(response, JSON.toJSONString(result));
    }
}
```

异常统一返回效果：
```json
{"code": 401, "msg": "需要登录后操作", "data": null}
{"code": 505, "msg": "用户名或密码错误", "data": null}
{"code": 508, "msg": "用户名不能为空", "data": null}
```

---

## 七、AOP 日志

**文件**：`blog/.../annotation/SystemLog.java` + `blog/.../aspect/LogAspect.java`

### 7.1 注解定义

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SystemLog {
    String businessName();  // 业务名称，如"热门文章排行"
}
```

### 7.2 切面实现

```java
@Aspect
@Component
public class LogAspect {
    @Pointcut("@annotation(com.xyzy.annotation.SystemLog)")
    public void pt() {}

    @Around("pt()")
    public Object printLog(ProceedingJoinPoint joinPoint) throws Throwable {
        // 前置：打印 URL、方法、参数等
        handleBefore(joinPoint);
        Object ret = joinPoint.proceed();  // 执行目标方法
        // 后置：打印返回值
        handleAfter(ret);
        return ret;
    }

    private void handleBefore(ProceedingJoinPoint joinPoint) {
        HttpServletRequest request = ...;
        SystemLog systemLog = getSystemLog(joinPoint);
        log.info("URL           : {}", request.getRequestURL());
        log.info("BusinessName  : {}", systemLog.businessName());
        log.info("HTTP Method   : {}", request.getMethod());
        log.info("Class Method  : {}.{}", ...);
        log.info("IP            : {}", request.getRemoteHost());
        log.info("Request Args  : {}", JSON.toJSONString(joinPoint.getArgs()));
    }

    private void handleAfter(Object ret) {
        log.info("Response      : {}", JSON.toJSONString(ret));
    }
}
```

### 7.3 使用方式

在任意 Controller 方法上加注解即可：
```java
@SystemLog(businessName = "热门文章排行")
@GetMapping("/hotArticleList")
public ResponseResult hotArticleList() { ... }
```

控制台输出示例：
```
=======Start=======
URL           : http://localhost:7777/article/hotArticleList
BusinessName  : 热门文章排行
HTTP Method   : GET
Class Method  : com.xyzy.controller.ArticleController.hotArticleList
IP            : 127.0.0.1
Request Args  : []
Response      : {"code":200,"msg":"操作成功","data":[...]}
=======End=======
```

---

## 八、Redis 浏览量 + 定时任务

### 8.1 项目启动：加载 MySQL → Redis

**文件**：`blog/.../runner/ViewCountRunner.java`

`CommandLineRunner` 会在 Spring Boot 启动完成后自动执行：

```java
@Component
public class ViewCountRunner implements CommandLineRunner {
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private RedisCache redisCache;

    @Override
    public void run(String... args) {
        // 查所有文章的 id 和 viewCount
        List<Article> articles = articleMapper.selectList(null);
        Map<String, Long> map = articles.stream()
            .collect(Collectors.toMap(a -> a.getId().toString(), Article::getViewCount));

        // 一次性写入 Redis Hash：article:viewCount → { "1": 1520, "2": 890, "3": 2300 }
        redisCache.setCacheMap(SystemConstants.REDIS_ARTICLE_VIEWCOUNT, map);
    }
}
```

### 8.2 阅读文章：Redis +1

每次访问文章详情时，调用 `PUT /article/updateViewCount/{id}`：

```java
redisCache.incrementCacheMapValue("article:viewCount", "1", 1);
// Redis 执行: HINCRBY article:viewCount 1 1
```

### 8.3 定时同步：Redis → MySQL

**文件**：`blog/.../job/UpdateViewCountJob.java`

```java
@Component
public class UpdateViewCountJob {
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private ArticleService articleService;

    @Scheduled(cron = "0 */5 * * * ?")   // 每 5 分钟执行
    public void updateViewCount() {
        // 从 Redis 拿所有文章的浏览量
        Map<String, Long> map = redisCache.getCacheMap("article:viewCount");

        // 逐条更新回 MySQL
        map.forEach((idStr, viewCount) -> {
            Article article = new Article();
            article.setId(Long.valueOf(idStr));
            article.setViewCount(viewCount);
            articleService.updateById(article);     // UPDATE t_article SET view_count=? WHERE id=?
        });
    }
}
```

启动类需要 `@EnableScheduling` 才能触发定时任务。

### 8.4 Redis 配置

**文件**：`framework/.../config/RedisConfig.java`

```java
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate template = new RedisTemplate();
        template.setConnectionFactory(factory);
        // Key 用 String 序列化（防止乱码）
        StringRedisSerializer serializer = new StringRedisSerializer();
        template.setKeySerializer(serializer);
        template.setHashKeySerializer(serializer);
        return template;
    }
}
```

**文件**：`framework/.../utils/RedisCache.java`

封装了 `RedisTemplate` 的常用操作：
- `setCacheObject(key, value)` — 存 String
- `getCacheObject(key)` — 取 String
- `setCacheMap(key, map)` — 存 Hash
- `getCacheMap(key)` — 取 Hash
- `incrementCacheMapValue(key, hashKey, delta)` — Hash 自增

---

## 九、数据库表结构

`sql/init.sql` 包含 11 张表：

```
t_article       文章表（title, content, summary, category_id, view_count, status, is_top...）
t_category      分类表（name, pid, description, status）
t_comment       评论表（type, article_id, root_id, content, to_comment_user_id, to_comment_id）
t_link          友链表（name, logo, description, address, status）
t_tag           标签表（name, remark）
t_article_tag   文章-标签关联表
t_user          用户表（user_name, nick_name, password, email, type, status）
t_role          角色表（role_name, role_key）
t_menu          菜单表（menu_name, parent_id, path, perms）
t_user_role     用户-角色关联表
t_role_menu     角色-菜单关联表
```

建表特点：
- 全部用 `id BIGINT AUTO_INCREMENT PRIMARY KEY`
- 全部有 `del_flag` 逻辑删除字段（配合 MyBatis-Plus `@TableLogic`）
- 全部有 `create_by` / `create_time` / `update_by` / `update_time` 审计字段（配合 `MyMetaObjectHandler` 自动填充）
- RBAC 五表实现用户→角色→菜单的权限模型

---

## 十、Postman 测试

项目根目录提供了 `XyzyBlog-Blog.postman_collection.json`，包含全部 14 个前台接口。

导入后：
1. 先调「用户注册」或使用测试数据 `admin / admin123`
2. 调「登录」，token 自动存入 Collection 变量
3. 需要登录的接口自动携带 token
4. 每个接口都绑定了测试脚本，自动校验状态码和数据格式

---

## 十一、完整文件索引

```
XyzyBlog
├── pom.xml                                     父 POM（管理依赖版本）
├── README.md                                   本文件
├── XyzyBlog-Blog.postman_collection.json       Postman 接口集合
├── sql/init.sql                                数据库建库建表 + 测试数据
├── deploy/docker-compose.yml                   Redis 容器
│
├── framework/                                  公共模块
│   └── src/main/java/com/xyzy/
│       ├── domain/
│       │   ├── ResponseResult.java             统一返回类
│       │   ├── entity/
│       │   │   ├── Article.java                t_article
│       │   │   ├── Category.java               t_category
│       │   │   ├── Comment.java                t_comment
│       │   │   ├── Link.java                   t_link
│       │   │   ├── User.java                   t_user
│       │   │   ├── LoginUser.java              UserDetails 实现（密码+权限）
│       │   │   ├── Tag.java                    t_tag
│       │   │   ├── ArticleTag.java             t_article_tag
│       │   │   ├── Role.java                   t_role
│       │   │   ├── Menu.java                   t_menu
│       │   │   ├── UserRole.java               t_user_role
│       │   │   └── RoleMenu.java               t_role_menu
│       │   ├── vo/
│       │   │   ├── HotArticleVo.java           热门文章 VO
│       │   │   ├── ArticleListVo.java          文章列表 VO
│       │   │   ├── ArticleDetailVo.java        文章详情 VO
│       │   │   ├── CategoryVo.java             分类 VO
│       │   │   ├── LinkVo.java                 友链 VO
│       │   │   ├── CommentVo.java              评论 VO（含 children 树）
│       │   │   ├── PageVo.java                 分页 VO（rows + total）
│       │   │   ├── BlogUserLoginVo.java        登录返回 VO（token + userInfo）
│       │   │   ├── UserInfoVo.java             用户信息 VO
│       │   │   ├── AdminLoginVo.java           后台登录 VO
│       │   │   └── MenuTreeVo.java             菜单树 VO
│       │   └── dto/
│       │       ├── AddCommentDto.java          发表评论入参
│       │       ├── LoginRequest.java           登录入参
│       │       └── ...WriteRequest.java        CRUD 操作的入参
│       ├── mapper/
│       │   ├── ArticleMapper.java              文章 Mapper
│       │   ├── CategoryMapper.java             分类 Mapper
│       │   ├── CommentMapper.java              评论 Mapper
│       │   ├── LinkMapper.java                 友链 Mapper
│       │   ├── UserMapper.java                 用户 Mapper
│       │   ├── MenuMapper.java                 菜单 Mapper（含 selectPermsByUserId）
│       │   ├── RoleMapper.java                 角色 Mapper
│       │   ├── TagMapper.java                  标签 Mapper
│       │   ├── ArticleTagMapper.java           文章标签关联 Mapper
│       │   ├── UserRoleMapper.java             用户角色关联 Mapper
│       │   └── RoleMenuMapper.java             角色菜单关联 Mapper
│       ├── service/
│       │   ├── ArticleService.java             + impl/ArticleServiceImpl.java
│       │   ├── CategoryService.java            + impl/CategoryServiceImpl.java
│       │   ├── CommentService.java             + impl/CommentServiceImpl.java
│       │   ├── LinkService.java                + impl/LinkServiceImpl.java
│       │   ├── UserService.java                + impl/UserServiceImpl.java
│       │   ├── BlogLoginService.java           + impl/BlogLoginServiceImpl.java
│       │   ├── AdminContentService.java        后台内容管理
│       │   ├── AdminSystemService.java         后台系统管理
│       │   └── impl/UserDetailsServiceImpl.java  Spring Security 查用户
│       ├── handler/
│       │   ├── GlobalExceptionHandler.java     全局异常处理
│       │   ├── AuthenticationEntryPointImpl.java  未登录处理（401）
│       │   ├── AccessDeniedHandlerImpl.java       无权限处理（403）
│       │   └── MyMetaObjectHandler.java       自动填充 createTime/updateTime
│       ├── config/
│       │   └── RedisConfig.java               Redis 序列化配置
│       ├── utils/
│       │   ├── JwtUtil.java                   JWT 生成/解析
│       │   ├── RedisCache.java                Redis 操作封装
│       │   ├── BeanCopyUtils.java             Entity ↔ VO 拷贝
│       │   ├── SecurityUtils.java             从 SecurityContext 取用户
│       │   ├── WebUtils.java                  响应写入工具
│       │   ├── FastJsonRedisSerializer.java   FastJson 序列化器
│       │   └── PathUtils.java                 路径工具
│       ├── enums/
│       │   └── AppHttpCodeEnum.java           状态码枚举（200/401/403/500/501...）
│       ├── constants/
│       │   └── SystemConstants.java           系统常量
│       ├── exception/
│       │   └── SystemException.java           自定义业务异常
│       └── security/
│           └── LoginSessionService.java       登录会话管理
│
├── blog/                                       前台模块（端口 7777）
│   └── src/main/java/com/xyzy/
│       ├── BlogApplication.java                启动类（@MapperScan + @EnableScheduling）
│       ├── controller/
│       │   ├── ArticleController.java          文章接口
│       │   ├── CategoryController.java         分类接口
│       │   ├── CommentController.java          评论接口
│       │   ├── LinkController.java             友链接口
│       │   ├── BlogLoginController.java        登录/退出接口
│       │   └── UserController.java             用户接口（注册/个人信息）
│       ├── config/
│       │   ├── SecurityConfig.java             Spring Security 配置
│       │   └── MyBatisPlusConfig.java          分页插件配置
│       ├── filter/
│       │   └── JwtAuthenticationTokenFilter.java  JWT 认证过滤器
│       ├── aspect/
│       │   └── LogAspect.java                  AOP 日志切面
│       ├── annotation/
│       │   └── SystemLog.java                  日志注解
│       ├── runner/
│       │   └── ViewCountRunner.java            启动时加载浏览量到 Redis
│       └── job/
│           └── UpdateViewCountJob.java         定时同步浏览量
│
└── admin/                                      后台模块（端口 8989）
    └── src/main/java/com/xyzy/
        ├── AdminApplication.java               启动类
        ├── controller/
        │   ├── AdminArticleController.java     文章管理
        │   ├── AdminCategoryController.java    分类管理
        │   ├── AdminTagController.java         标签管理
        │   ├── AdminLinkController.java        友链管理
        │   ├── AdminUserController.java        用户管理
        │   ├── AdminRoleController.java        角色管理
        │   ├── AdminMenuController.java        菜单管理
        │   └── AdminLoginController.java       后台登录
        ├── config/
        │   └── AdminSecurityConfig.java        后台 Security 配置
        ├── filter/
        │   └── AdminJwtAuthenticationTokenFilter.java  后台 JWT 过滤器
        └── service/
            └── AdminLoginService.java          后台登录服务
```

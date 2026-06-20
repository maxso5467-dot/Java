# XyzyBlog Admin Completion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete the empty `admin` module as a secured REST API, preserve the existing `blog` API, and deliver a tested, runnable, submission-ready multi-module project.

**Architecture:** Shared entities, mappers, services, validation, and security primitives live in `framework`; `blog` remains the public API on port 7777; `admin` becomes an independent Spring Boot application on port 8989 with controllers and method-level authorization. MySQL stores content and RBAC data, while Redis stores separate `bloglogin:` and `adminlogin:` sessions.

**Tech Stack:** Java 17, Spring Boot 3.3.5, Spring Security 6, MyBatis-Plus 3.5.7, MySQL, Redis, JUnit 5, Mockito, MockMvc, Maven.

---

## File Map

- `sql/init.sql`: complete schema and deterministic seed data.
- `framework/src/main/java/com/xyzy/domain/entity`: shared database entities.
- `framework/src/main/java/com/xyzy/domain/dto`: validated admin write requests.
- `framework/src/main/java/com/xyzy/domain/vo`: page, login, menu-tree, and detail responses.
- `framework/src/main/java/com/xyzy/mapper`: MyBatis-Plus CRUD and RBAC queries.
- `framework/src/main/java/com/xyzy/service`: content and system-management contracts.
- `framework/src/main/java/com/xyzy/service/impl`: transactional implementations.
- `framework/src/main/java/com/xyzy/security`: reusable JWT session resolution and permission loading.
- `admin/src/main/java/com/xyzy`: admin application, configuration, filter, and controllers.
- `framework/src/test` and `admin/src/test`: isolated unit and MVC tests.
- `README.md` and `docs/test-results.md`: operating instructions and verification evidence.

### Task 1: Establish the complete schema and RBAC seed

**Files:**
- Modify: `sql/init.sql`
- Create: `framework/src/main/java/com/xyzy/domain/entity/Tag.java`
- Create: `framework/src/main/java/com/xyzy/domain/entity/ArticleTag.java`
- Create: `framework/src/main/java/com/xyzy/domain/entity/Role.java`
- Create: `framework/src/main/java/com/xyzy/domain/entity/Menu.java`
- Create: `framework/src/main/java/com/xyzy/domain/entity/UserRole.java`
- Create: `framework/src/main/java/com/xyzy/domain/entity/RoleMenu.java`
- Test: `framework/src/test/java/com/xyzy/domain/SchemaEntityTest.java`

- [ ] **Step 1: Write entity mapping tests**

```java
class SchemaEntityTest {
    @Test void mapsRbacTables() {
        assertEquals("t_role", Role.class.getAnnotation(TableName.class).value());
        assertEquals("t_menu", Menu.class.getAnnotation(TableName.class).value());
        assertEquals("t_tag", Tag.class.getAnnotation(TableName.class).value());
    }
}
```

- [ ] **Step 2: Run the test and confirm missing types fail compilation**

Run: `mvn -pl framework -am -Dtest=SchemaEntityTest test`

Expected: FAIL because `Role`, `Menu`, and `Tag` do not exist.

- [ ] **Step 3: Add six focused MyBatis-Plus entities**

Use `@TableName`, `@TableId(type = IdType.AUTO)`, audit fields, and `delFlag` only on business records. Join entities contain their two foreign-key IDs and no artificial behavior.

```java
@Data
@TableName("t_role")
public class Role {
    @TableId(type = IdType.AUTO) private Long id;
    private String roleName;
    private String roleKey;
    private Integer roleSort;
    private String status;
    private Long createBy;
    private Date createTime;
    private Long updateBy;
    private Date updateTime;
    private Integer delFlag;
}
```

- [ ] **Step 4: Extend `init.sql` with ordered drops, tables, indexes, and seed permissions**

Create `t_tag`, `t_article_tag`, `t_role`, `t_menu`, `t_user_role`, and `t_role_menu`. Seed administrator role key `admin`, management permissions such as `content:article:list` and `system:user:edit`, and assign user 1 to the role.

- [ ] **Step 5: Run entity tests**

Run: `mvn -pl framework -am -Dtest=SchemaEntityTest test`

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add sql/init.sql framework/src/main framework/src/test
git commit -m "feat: add blog RBAC and tag schema"
```

### Task 2: Build reusable permission-aware authentication

**Files:**
- Modify: `framework/src/main/java/com/xyzy/domain/entity/LoginUser.java`
- Modify: `framework/src/main/java/com/xyzy/service/impl/UserDetailsServiceImpl.java`
- Create: `framework/src/main/java/com/xyzy/mapper/MenuMapper.java`
- Create: `framework/src/main/java/com/xyzy/mapper/RoleMapper.java`
- Create: `framework/src/main/java/com/xyzy/security/LoginSessionService.java`
- Test: `framework/src/test/java/com/xyzy/security/LoginSessionServiceTest.java`

- [ ] **Step 1: Write failing authority and session tests**

```java
@Test void convertsPermissionStringsToAuthorities() {
    LoginUser user = new LoginUser(new User(), List.of("system:user:list"));
    assertEquals("system:user:list", user.getAuthorities().iterator().next().getAuthority());
}
```

- [ ] **Step 2: Verify failure**

Run: `mvn -pl framework -am -Dtest=LoginSessionServiceTest test`

Expected: FAIL because `getAuthorities()` currently returns null and session service does not exist.

- [ ] **Step 3: Implement permission loading**

`MenuMapper.selectPermsByUserId(long)` joins user-role, role-menu, role, and menu, filters active records, and returns nonblank `perms`. Administrators receive all seeded permissions through the same query.

- [ ] **Step 4: Implement granted authorities and scoped Redis sessions**

```java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    return permissions == null ? List.of() : permissions.stream()
        .map(SimpleGrantedAuthority::new)
        .toList();
}
```

`LoginSessionService` reads/writes/deletes `{scope}login:{userId}`, where scope is `blog` or `admin`, and rejects disabled/deleted users.

- [ ] **Step 5: Run tests and commit**

Run: `mvn -pl framework -am -Dtest=LoginSessionServiceTest test`

Expected: PASS.

```bash
git add framework
git commit -m "feat: add reusable RBAC login sessions"
```

### Task 3: Make the admin application independently runnable

**Files:**
- Create: `admin/src/main/java/com/xyzy/AdminApplication.java`
- Create: `admin/src/main/java/com/xyzy/config/AdminSecurityConfig.java`
- Create: `admin/src/main/java/com/xyzy/filter/AdminJwtAuthenticationTokenFilter.java`
- Create: `admin/src/main/resources/application.yml`
- Create: `admin/src/main/java/com/xyzy/controller/AdminLoginController.java`
- Create: `framework/src/main/java/com/xyzy/domain/dto/LoginRequest.java`
- Create: `framework/src/main/java/com/xyzy/domain/vo/AdminLoginVo.java`
- Test: `admin/src/test/java/com/xyzy/controller/AdminLoginControllerTest.java`

- [ ] **Step 1: Write MVC tests for anonymous login and protected endpoints**

```java
@Test void rejectsAnonymousManagementRequest() throws Exception {
    mvc.perform(get("/system/user/list"))
        .andExpect(status().isUnauthorized());
}
```

- [ ] **Step 2: Confirm the empty module cannot run tests**

Run: `mvn -pl admin -am -Dtest=AdminLoginControllerTest test`

Expected: FAIL because the application and controller do not exist.

- [ ] **Step 3: Add application and configuration**

`AdminApplication` uses `@SpringBootApplication` and `@MapperScan("com.xyzy.mapper")`. Configure port 8989, the same environment-based MySQL/Redis settings as blog, stateless security, `/login` as anonymous, `/logout` as authenticated, and all other routes as authenticated.

- [ ] **Step 4: Add admin JWT filter and login endpoints**

Use Redis key prefix `adminlogin:`. Endpoints are `POST /login`, `POST /logout`, and `GET /getInfo`. Successful login returns token, user data, roles, and permissions without a password.

- [ ] **Step 5: Run tests and package the reactor**

Run: `mvn -pl admin -am test`

Expected: PASS.

Run: `mvn clean package -DskipTests`

Expected: all four reactor modules SUCCESS.

- [ ] **Step 6: Commit**

```bash
git add admin framework
git commit -m "feat: make admin a secured Spring Boot application"
```

### Task 4: Implement content-management services and controllers

**Files:**
- Create: `framework/src/main/java/com/xyzy/mapper/TagMapper.java`
- Create: `framework/src/main/java/com/xyzy/mapper/ArticleTagMapper.java`
- Create: `framework/src/main/java/com/xyzy/service/AdminContentService.java`
- Create: `framework/src/main/java/com/xyzy/service/impl/AdminContentServiceImpl.java`
- Create: `framework/src/main/java/com/xyzy/domain/dto/ArticleWriteRequest.java`
- Create: `framework/src/main/java/com/xyzy/domain/dto/CategoryWriteRequest.java`
- Create: `framework/src/main/java/com/xyzy/domain/dto/TagWriteRequest.java`
- Create: `framework/src/main/java/com/xyzy/domain/dto/LinkWriteRequest.java`
- Create: `admin/src/main/java/com/xyzy/controller/AdminArticleController.java`
- Create: `admin/src/main/java/com/xyzy/controller/AdminCategoryController.java`
- Create: `admin/src/main/java/com/xyzy/controller/AdminTagController.java`
- Create: `admin/src/main/java/com/xyzy/controller/AdminLinkController.java`
- Test: `framework/src/test/java/com/xyzy/service/AdminContentServiceTest.java`
- Test: `admin/src/test/java/com/xyzy/controller/AdminContentControllerTest.java`

- [ ] **Step 1: Write failing service tests**

Test that article save replaces article-tag rows in one transaction, duplicate tag/category names return business errors, and deleting an in-use category is rejected.

```java
@Test void rejectsDeletingCategoryUsedByArticle() {
    when(articleMapper.selectCount(any())).thenReturn(1L);
    assertThrows(SystemException.class, () -> service.deleteCategory(2L));
}
```

- [ ] **Step 2: Verify tests fail**

Run: `mvn -pl framework -am -Dtest=AdminContentServiceTest test`

Expected: FAIL because admin content service does not exist.

- [ ] **Step 3: Implement transactional content service**

Provide page/detail/create/update/delete/status methods for articles, categories, tags, and links. Use `PageVo`, MyBatis-Plus wrappers, `@Transactional`, DTO validation, and logical deletion. Article writes synchronize `t_article_tag` and preserve Redis view counts.

- [ ] **Step 4: Add permission-protected controllers**

Use `/content/article`, `/content/category`, `/content/tag`, and `/content/link`; use `@PreAuthorize("hasAuthority('content:article:list')")` style checks. Return existing `ResponseResult` consistently.

- [ ] **Step 5: Run service and MVC tests**

Run: `mvn -pl admin -am test`

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add framework admin
git commit -m "feat: add admin content management APIs"
```

### Task 5: Implement user, role, and menu management

**Files:**
- Create: `framework/src/main/java/com/xyzy/mapper/UserRoleMapper.java`
- Create: `framework/src/main/java/com/xyzy/mapper/RoleMenuMapper.java`
- Create: `framework/src/main/java/com/xyzy/service/AdminSystemService.java`
- Create: `framework/src/main/java/com/xyzy/service/impl/AdminSystemServiceImpl.java`
- Create: `framework/src/main/java/com/xyzy/domain/dto/AdminUserWriteRequest.java`
- Create: `framework/src/main/java/com/xyzy/domain/dto/RoleWriteRequest.java`
- Create: `framework/src/main/java/com/xyzy/domain/dto/MenuWriteRequest.java`
- Create: `framework/src/main/java/com/xyzy/domain/vo/MenuTreeVo.java`
- Create: `admin/src/main/java/com/xyzy/controller/AdminUserController.java`
- Create: `admin/src/main/java/com/xyzy/controller/AdminRoleController.java`
- Create: `admin/src/main/java/com/xyzy/controller/AdminMenuController.java`
- Test: `framework/src/test/java/com/xyzy/service/AdminSystemServiceTest.java`
- Test: `admin/src/test/java/com/xyzy/controller/AdminSystemControllerTest.java`

- [ ] **Step 1: Write failing RBAC mutation tests**

Test BCrypt password creation, uniqueness checks, user-role replacement, role-menu replacement, prevention of deleting user 1 or the administrator role, and recursive menu trees.

- [ ] **Step 2: Verify failure**

Run: `mvn -pl framework -am -Dtest=AdminSystemServiceTest test`

Expected: FAIL because system management service does not exist.

- [ ] **Step 3: Implement the system service**

All relationship replacements run inside transactions: delete old join rows, insert distinct IDs, then invalidate affected `adminlogin:` sessions. User responses clear passwords. Menu parent checks prevent self-parenting and cycles.

- [ ] **Step 4: Add system controllers and permissions**

Expose `/system/user`, `/system/role`, and `/system/menu` CRUD, status, role assignment, and menu assignment operations. Protect each operation with matching seeded permissions.

- [ ] **Step 5: Run tests and commit**

Run: `mvn -pl admin -am test`

Expected: PASS.

```bash
git add framework admin
git commit -m "feat: add user role and menu management"
```

### Task 6: Harden validation, errors, and regression coverage

**Files:**
- Modify: `framework/pom.xml`
- Modify: `framework/src/main/java/com/xyzy/enums/AppHttpCodeEnum.java`
- Modify: `framework/src/main/java/com/xyzy/handler/GlobalExceptionHandler.java`
- Modify: `framework/src/main/java/com/xyzy/service/impl/UserDetailsServiceImpl.java`
- Test: `framework/src/test/java/com/xyzy/handler/GlobalExceptionHandlerTest.java`
- Test: `blog/src/test/java/com/xyzy/BlogRegressionTest.java`

- [ ] **Step 1: Write failing validation and regression tests**

Assert invalid DTOs return code 400 without stack details; missing resources return 404-style business responses; disabled users cannot authenticate; public blog routes remain available without authentication.

- [ ] **Step 2: Add validation dependency and handlers**

Add `spring-boot-starter-validation`. Handle `MethodArgumentNotValidException`, `BindException`, malformed JSON, duplicate keys, and business exceptions with stable messages. Log internal exceptions server-side and return generic system errors.

- [ ] **Step 3: Run the full test suite**

Run: `mvn clean test`

Expected: BUILD SUCCESS with framework, blog, and admin tests executed.

- [ ] **Step 4: Commit**

```bash
git add framework blog admin
git commit -m "test: harden validation and preserve blog behavior"
```

### Task 7: Verify with MySQL and Redis

**Files:**
- Create: `deploy/docker-compose.yml`
- Create: `tools/smoke-test.ps1`
- Create: `docs/test-results.md`

- [ ] **Step 1: Add local Redis fallback**

Create a Compose file containing Redis 7 on port 6379. Do not define MySQL because the machine already provides MySQL on 3306.

- [ ] **Step 2: Add deterministic smoke test script**

The script waits for both health endpoints/routes, logs in with the seeded administrator, calls public blog routes, and performs create/read/update/delete cycles for one category, tag, link, article, user, role, and menu. It exits nonzero on HTTP or application-code failure and never prints the password or token.

- [ ] **Step 3: Initialize database and start services**

Use the installed MySQL service and import `sql/init.sql`. Start Redis with Docker Compose if port 6379 is not listening. Launch `blog` and `admin` JARs in hidden background processes.

- [ ] **Step 4: Run smoke tests**

Run: `powershell -ExecutionPolicy Bypass -File tools/smoke-test.ps1`

Expected: every named scenario reports PASS and the process exits 0.

- [ ] **Step 5: Record sanitized evidence and commit**

Record Java/Maven versions, test counts, build result, service ports, and smoke scenario results in `docs/test-results.md`.

```bash
git add deploy tools docs/test-results.md
git commit -m "test: add real environment smoke verification"
```

### Task 8: Documentation and submission package

**Files:**
- Modify: `README.md`
- Create: `tools/package-release.ps1`
- Output: `release/XyzyBlog-source-1.0.0.zip`

- [ ] **Step 1: Update operating documentation**

Document prerequisites, environment variables, clean database initialization, Redis startup, blog/admin launch commands, seeded test account, route summary, automated tests, and troubleshooting. Do not include local absolute paths or plaintext secrets beyond the explicitly documented local test credential.

- [ ] **Step 2: Add reproducible packaging script**

Package tracked source and documentation while excluding `.git`, `.idea`, `.m2`, `target`, `release`, logs, and machine-local settings. Validate that the archive contains root/module POMs, SQL, README, and test report.

- [ ] **Step 3: Final verification**

Run: `mvn clean test`

Expected: BUILD SUCCESS.

Run: `mvn clean package`

Expected: BUILD SUCCESS and executable blog/admin JARs.

Run: `powershell -ExecutionPolicy Bypass -File tools/package-release.ps1`

Expected: `release/XyzyBlog-source-1.0.0.zip` exists and contains no excluded paths.

- [ ] **Step 4: Commit**

```bash
git add README.md tools/package-release.ps1 docs/test-results.md
git commit -m "docs: finalize XyzyBlog submission package"
```


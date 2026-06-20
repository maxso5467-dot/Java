# XyzyBlog

XyzyBlog 是一个基于 Spring Boot 3 的个人博客后端 API 项目，提供文章、分类、评论、友情链接、用户注册登录和文章浏览量统计等功能。

## 技术栈

- Java 17
- Spring Boot 3.3.5
- Spring Security + JWT
- MyBatis-Plus
- MySQL
- Redis

## 本地环境

运行项目前需要准备：

1. JDK 17
2. Maven 3.9 或使用 IntelliJ IDEA 自带的 Maven
3. MySQL，端口 `3306`
4. Redis，端口 `6379`

默认数据库配置：

```text
数据库：myblog
用户名：root
密码：123456
```

如果本地配置不同，可以修改 `blog/src/main/resources/application.yml`，或者设置其中对应的环境变量。

## 初始化数据库

在 MySQL 中执行：

```text
sql/init.sql
```

该脚本会创建 `myblog` 数据库、业务表和测试数据。

## IDEA 启动

1. 使用 IntelliJ IDEA 打开项目根目录。
2. 等待 Maven 下载依赖。
3. 将 Project SDK 设置为 JDK 17。
4. 确认 MySQL 和 Redis 已启动。
5. 运行 `blog/src/main/java/com/xyzy/BlogApplication.java`。
6. 启动成功后，API 地址为 `http://localhost:7777`。

也可以使用 Maven 构建博客模块：

```bash
mvn -pl blog -am clean package -DskipTests
```

## 测试接口

浏览器可以直接访问：

```text
http://localhost:7777/category/getCategoryList
http://localhost:7777/article/hotArticleList
http://localhost:7777/article/articleList?pageNum=1&pageSize=10
http://localhost:7777/article/1
http://localhost:7777/link/getAllLink
```

Apifox 的环境前置 URL 设置为：

```text
http://localhost:7777
```

## 项目结构

```text
admin       预留的后台管理模块，目前未实现
blog        博客 API 和 Spring Boot 启动模块
framework   实体、Mapper、Service、安全和公共工具
sql         数据库初始化脚本
```

## 前端说明

当前仓库没有 Vue、React 或 HTML 前端页面。运行后提供的是 JSON API，需要使用浏览器或 Apifox 访问接口。如果需要可视化博客页面，还需要单独开发或加入前端项目。

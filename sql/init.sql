-- XyzyBlog Database
CREATE DATABASE IF NOT EXISTS myblog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE myblog;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS t_role_menu;
DROP TABLE IF EXISTS t_user_role;
DROP TABLE IF EXISTS t_article_tag;
DROP TABLE IF EXISTS t_menu;
DROP TABLE IF EXISTS t_role;
DROP TABLE IF EXISTS t_tag;
DROP TABLE IF EXISTS t_article;
CREATE TABLE t_article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(256) DEFAULT NULL COMMENT '标题',
    content LONGTEXT COMMENT '文章内容',
    summary VARCHAR(1024) DEFAULT NULL COMMENT '文章摘要',
    category_id BIGINT DEFAULT NULL COMMENT '所属分类id',
    thumbnail VARCHAR(256) DEFAULT NULL COMMENT '缩略图',
    is_top CHAR(1) DEFAULT '0' COMMENT '是否置顶(0否 1是)',
    status CHAR(1) DEFAULT '1' COMMENT '状态(0已发布 1草稿)',
    view_count BIGINT DEFAULT 0 COMMENT '访问量',
    is_comment CHAR(1) DEFAULT '1' COMMENT '是否允许评论(1是 0否)',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT NULL,
    del_flag INT DEFAULT 0 COMMENT '删除标志(0未删除 1已删除)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

DROP TABLE IF EXISTS t_category;
CREATE TABLE t_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) DEFAULT NULL COMMENT '分类名',
    pid BIGINT DEFAULT -1 COMMENT '父分类id',
    description VARCHAR(512) DEFAULT NULL COMMENT '描述',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT NULL,
    del_flag INT DEFAULT 0 COMMENT '删除标志(0未删除 1已删除)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

DROP TABLE IF EXISTS t_comment;
CREATE TABLE t_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type CHAR(1) DEFAULT '0' COMMENT '评论类型(0文章评论 1友链评论)',
    article_id BIGINT DEFAULT NULL COMMENT '文章id',
    root_id BIGINT DEFAULT -1 COMMENT '根评论id',
    content VARCHAR(512) DEFAULT NULL COMMENT '评论内容',
    to_comment_user_id BIGINT DEFAULT -1 COMMENT '所回复的目标评论的userid',
    to_comment_id BIGINT DEFAULT -1 COMMENT '回复目标评论id',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT NULL,
    del_flag INT DEFAULT 0 COMMENT '删除标志(0未删除 1已删除)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

DROP TABLE IF EXISTS t_link;
CREATE TABLE t_link (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(256) DEFAULT NULL COMMENT '网站名',
    logo VARCHAR(256) DEFAULT NULL COMMENT 'logo',
    description VARCHAR(512) DEFAULT NULL COMMENT '描述',
    address VARCHAR(128) DEFAULT NULL COMMENT '网站地址',
    status CHAR(1) DEFAULT '0' COMMENT '审核状态(0通过 1未通过 2未审核)',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT NULL,
    del_flag INT DEFAULT 0 COMMENT '删除标志(0未删除 1已删除)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='友链表';

DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(64) DEFAULT NULL COMMENT '用户名',
    nick_name VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    password VARCHAR(64) DEFAULT NULL COMMENT '密码',
    type CHAR(1) DEFAULT '0' COMMENT '用户类型(0普通用户 1管理员)',
    status CHAR(1) DEFAULT '0' COMMENT '账号状态(0正常 1停用)',
    email VARCHAR(64) DEFAULT NULL COMMENT '邮箱',
    phonenumber VARCHAR(32) DEFAULT NULL COMMENT '手机号',
    sex CHAR(1) DEFAULT NULL COMMENT '用户性别(0男 1女 2未知)',
    avatar VARCHAR(256) DEFAULT NULL COMMENT '头像',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT NULL,
    del_flag INT DEFAULT 0 COMMENT '删除标志(0未删除 1已删除)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE t_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL COMMENT '标签名',
    remark VARCHAR(512) DEFAULT NULL COMMENT '备注',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT NULL,
    del_flag INT DEFAULT 0 COMMENT '删除标志',
    UNIQUE KEY uk_tag_name (name, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

CREATE TABLE t_article_tag (
    article_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (article_id, tag_id),
    KEY idx_article_tag_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联表';

CREATE TABLE t_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(64) NOT NULL COMMENT '角色权限字符串',
    role_sort INT DEFAULT 0 COMMENT '显示顺序',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT NULL,
    del_flag INT DEFAULT 0 COMMENT '删除标志',
    UNIQUE KEY uk_role_key (role_key, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE t_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_name VARCHAR(64) NOT NULL COMMENT '菜单名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    path VARCHAR(200) DEFAULT '' COMMENT '路由地址',
    component VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
    menu_type CHAR(1) DEFAULT 'C' COMMENT '类型(M目录 C菜单 F按钮)',
    visible CHAR(1) DEFAULT '0' COMMENT '显示状态',
    status CHAR(1) DEFAULT '0' COMMENT '菜单状态',
    perms VARCHAR(128) DEFAULT NULL COMMENT '权限标识',
    icon VARCHAR(100) DEFAULT '#' COMMENT '图标',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT NULL,
    del_flag INT DEFAULT 0 COMMENT '删除标志',
    KEY idx_menu_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

CREATE TABLE t_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    KEY idx_user_role_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE t_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id),
    KEY idx_role_menu_menu (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 插入测试数据
INSERT INTO t_user (id, user_name, nick_name, password, type, status, email, phonenumber, sex, create_time, update_time) VALUES
(1, 'admin', '管理员', '$2a$10$zeVhaGiCJexFEuXyiuORRORy5UyEE5yvj7Os/Sy3xctcjh8szbgfe', '1', '0', 'admin@xyzy.com', '13800138000', '0', NOW(), NOW());

INSERT INTO t_category (id, name, pid, description, status, create_time, update_time) VALUES
(1, 'Java', -1, 'Java相关文章', '0', NOW(), NOW()),
(2, 'Spring Boot', -1, 'Spring Boot相关', '0', NOW(), NOW()),
(3, '数据库', -1, '数据库相关', '0', NOW(), NOW());

INSERT INTO t_article (id, title, content, summary, category_id, thumbnail, is_top, status, view_count, is_comment, create_by, create_time, update_by, update_time) VALUES
(1, 'Spring Boot入门教程', 'Spring Boot 是由 Pivotal 团队提供的全新框架...', '本文介绍Spring Boot基础入门知识', 2, 'https://picsum.photos/800/400', '1', '0', 1520, '0', 1, NOW(), 1, NOW()),
(2, 'Java集合框架详解', 'Java集合框架是Java编程中最常用的工具之一...', '深入理解Java集合框架', 1, 'https://picsum.photos/800/400', '0', '0', 890, '0', 1, NOW(), 1, NOW()),
(3, 'MySQL索引优化实践', '索引是提高MySQL查询性能的重要手段...', 'MySQL索引优化最佳实践', 3, 'https://picsum.photos/800/400', '0', '0', 2300, '0', 1, NOW(), 1, NOW());

INSERT INTO t_comment (id, type, article_id, root_id, content, to_comment_user_id, to_comment_id, create_by, create_time) VALUES
(1, '0', 1, -1, '写得很详细，学习了！', -1, -1, 1, NOW()),
(2, '0', 1, 1, '感谢支持！', 1, 1, 1, NOW());

INSERT INTO t_link (id, name, logo, description, address, status, create_time, update_time) VALUES
(1, 'GitHub', 'https://github.githubassets.com/favicons/favicon.svg', '全球最大的开源社区', 'https://github.com', '0', NOW(), NOW()),
(2, 'Stack Overflow', 'https://cdn.sstatic.net/Sites/stackoverflow/Img/favicon.ico', '技术问答社区', 'https://stackoverflow.com', '0', NOW(), NOW());

INSERT INTO t_tag (id, name, remark, create_by, create_time, update_by, update_time) VALUES
(1, 'Spring Boot', 'Spring Boot 技术', 1, NOW(), 1, NOW()),
(2, 'Java', 'Java 技术', 1, NOW(), 1, NOW()),
(3, 'MySQL', 'MySQL 技术', 1, NOW(), 1, NOW());

INSERT INTO t_article_tag (article_id, tag_id) VALUES (1, 1), (2, 2), (3, 3);

INSERT INTO t_role (id, role_name, role_key, role_sort, status, create_by, create_time, update_by, update_time) VALUES
(1, '超级管理员', 'admin', 1, '0', 1, NOW(), 1, NOW());

INSERT INTO t_menu (id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(1, '内容管理', 0, 1, 'content', NULL, 'M', '0', '0', NULL, 'document', 1, NOW()),
(2, '系统管理', 0, 2, 'system', NULL, 'M', '0', '0', NULL, 'system', 1, NOW()),
(101, '文章查询', 1, 1, '', NULL, 'F', '0', '0', 'content:article:list', '#', 1, NOW()),
(102, '文章新增', 1, 2, '', NULL, 'F', '0', '0', 'content:article:add', '#', 1, NOW()),
(103, '文章修改', 1, 3, '', NULL, 'F', '0', '0', 'content:article:edit', '#', 1, NOW()),
(104, '文章删除', 1, 4, '', NULL, 'F', '0', '0', 'content:article:remove', '#', 1, NOW()),
(111, '分类管理', 1, 5, '', NULL, 'F', '0', '0', 'content:category:manage', '#', 1, NOW()),
(112, '标签管理', 1, 6, '', NULL, 'F', '0', '0', 'content:tag:manage', '#', 1, NOW()),
(113, '友链管理', 1, 7, '', NULL, 'F', '0', '0', 'content:link:manage', '#', 1, NOW()),
(201, '用户管理', 2, 1, '', NULL, 'F', '0', '0', 'system:user:manage', '#', 1, NOW()),
(202, '角色管理', 2, 2, '', NULL, 'F', '0', '0', 'system:role:manage', '#', 1, NOW()),
(203, '菜单管理', 2, 3, '', NULL, 'F', '0', '0', 'system:menu:manage', '#', 1, NOW());

INSERT INTO t_user_role (user_id, role_id) VALUES (1, 1);
INSERT INTO t_role_menu (role_id, menu_id) SELECT 1, id FROM t_menu;

SET FOREIGN_KEY_CHECKS = 1;

COMMIT;

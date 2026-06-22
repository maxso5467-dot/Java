USE myblog;

DELIMITER //

DROP PROCEDURE IF EXISTS add_column_if_missing//
CREATE PROCEDURE add_column_if_missing(IN table_name_value VARCHAR(64), IN column_name_value VARCHAR(64), IN column_definition TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = table_name_value
          AND column_name = column_name_value
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', table_name_value, '` ADD COLUMN ', column_definition);
        PREPARE statement FROM @ddl;
        EXECUTE statement;
        DEALLOCATE PREPARE statement;
    END IF;
END//

DELIMITER ;

CALL add_column_if_missing('t_article', 'is_top', '`is_top` CHAR(1) DEFAULT ''0'' AFTER `thumbnail`');
CALL add_column_if_missing('t_article', 'is_comment', '`is_comment` CHAR(1) DEFAULT ''1'' AFTER `view_count`');
CALL add_column_if_missing('t_article', 'create_by', '`create_by` BIGINT DEFAULT NULL');
CALL add_column_if_missing('t_article', 'update_by', '`update_by` BIGINT DEFAULT NULL');
CALL add_column_if_missing('t_article', 'del_flag', '`del_flag` INT DEFAULT 0');

CALL add_column_if_missing('t_category', 'pid', '`pid` BIGINT DEFAULT -1 AFTER `name`');
CALL add_column_if_missing('t_category', 'create_by', '`create_by` BIGINT DEFAULT NULL');
CALL add_column_if_missing('t_category', 'create_time', '`create_time` DATETIME DEFAULT NULL');
CALL add_column_if_missing('t_category', 'update_by', '`update_by` BIGINT DEFAULT NULL');
CALL add_column_if_missing('t_category', 'update_time', '`update_time` DATETIME DEFAULT NULL');
CALL add_column_if_missing('t_category', 'del_flag', '`del_flag` INT DEFAULT 0');

CALL add_column_if_missing('t_comment', 'update_by', '`update_by` BIGINT DEFAULT NULL');
CALL add_column_if_missing('t_comment', 'del_flag', '`del_flag` INT DEFAULT 0');

CALL add_column_if_missing('t_link', 'create_by', '`create_by` BIGINT DEFAULT NULL');
CALL add_column_if_missing('t_link', 'create_time', '`create_time` DATETIME DEFAULT NULL');
CALL add_column_if_missing('t_link', 'update_by', '`update_by` BIGINT DEFAULT NULL');
CALL add_column_if_missing('t_link', 'update_time', '`update_time` DATETIME DEFAULT NULL');
CALL add_column_if_missing('t_link', 'del_flag', '`del_flag` INT DEFAULT 0');

CALL add_column_if_missing('t_user', 'type', '`type` CHAR(1) DEFAULT ''0'' AFTER `password`');
CALL add_column_if_missing('t_user', 'phonenumber', '`phonenumber` VARCHAR(32) DEFAULT NULL');
CALL add_column_if_missing('t_user', 'create_by', '`create_by` BIGINT DEFAULT NULL');
CALL add_column_if_missing('t_user', 'update_by', '`update_by` BIGINT DEFAULT NULL');
CALL add_column_if_missing('t_user', 'del_flag', '`del_flag` INT DEFAULT 0');

CALL add_column_if_missing('t_tag', 'remark', '`remark` VARCHAR(512) DEFAULT NULL');
CALL add_column_if_missing('t_tag', 'create_by', '`create_by` BIGINT DEFAULT NULL');
CALL add_column_if_missing('t_tag', 'create_time', '`create_time` DATETIME DEFAULT NULL');
CALL add_column_if_missing('t_tag', 'update_by', '`update_by` BIGINT DEFAULT NULL');
CALL add_column_if_missing('t_tag', 'update_time', '`update_time` DATETIME DEFAULT NULL');
CALL add_column_if_missing('t_tag', 'del_flag', '`del_flag` INT DEFAULT 0');

CALL add_column_if_missing('t_role', 'role_key', '`role_key` VARCHAR(64) NOT NULL DEFAULT ''''');
CALL add_column_if_missing('t_role', 'role_sort', '`role_sort` INT DEFAULT 0');
CALL add_column_if_missing('t_role', 'status', '`status` CHAR(1) DEFAULT ''0''');
CALL add_column_if_missing('t_role', 'create_by', '`create_by` BIGINT DEFAULT NULL');
CALL add_column_if_missing('t_role', 'create_time', '`create_time` DATETIME DEFAULT NULL');
CALL add_column_if_missing('t_role', 'update_by', '`update_by` BIGINT DEFAULT NULL');
CALL add_column_if_missing('t_role', 'update_time', '`update_time` DATETIME DEFAULT NULL');
CALL add_column_if_missing('t_role', 'del_flag', '`del_flag` INT DEFAULT 0');

CALL add_column_if_missing('t_menu', 'parent_id', '`parent_id` BIGINT DEFAULT 0');
CALL add_column_if_missing('t_menu', 'order_num', '`order_num` INT DEFAULT 0');
CALL add_column_if_missing('t_menu', 'component', '`component` VARCHAR(255) DEFAULT NULL');
CALL add_column_if_missing('t_menu', 'menu_type', '`menu_type` CHAR(1) DEFAULT ''C''');
CALL add_column_if_missing('t_menu', 'visible', '`visible` CHAR(1) DEFAULT ''0''');
CALL add_column_if_missing('t_menu', 'status', '`status` CHAR(1) DEFAULT ''0''');
CALL add_column_if_missing('t_menu', 'perms', '`perms` VARCHAR(128) DEFAULT NULL');
CALL add_column_if_missing('t_menu', 'icon', '`icon` VARCHAR(100) DEFAULT ''#''');
CALL add_column_if_missing('t_menu', 'create_by', '`create_by` BIGINT DEFAULT NULL');
CALL add_column_if_missing('t_menu', 'create_time', '`create_time` DATETIME DEFAULT NULL');
CALL add_column_if_missing('t_menu', 'update_by', '`update_by` BIGINT DEFAULT NULL');
CALL add_column_if_missing('t_menu', 'update_time', '`update_time` DATETIME DEFAULT NULL');
CALL add_column_if_missing('t_menu', 'del_flag', '`del_flag` INT DEFAULT 0');

UPDATE t_article SET del_flag = is_deleted WHERE is_deleted IS NOT NULL;
UPDATE t_comment SET del_flag = is_deleted WHERE is_deleted IS NOT NULL;
UPDATE t_user SET del_flag = is_deleted WHERE is_deleted IS NOT NULL;
UPDATE t_user
SET password = '$2a$10$zeVhaGiCJexFEuXyiuORRORy5UyEE5yvj7Os/Sy3xctcjh8szbgfe'
WHERE id = 1 AND password NOT LIKE '$2%';

INSERT INTO t_role (id, role_name, role_key, role_sort, status, create_by, create_time, update_by, update_time, del_flag)
VALUES (1, '超级管理员', 'admin', 1, '0', 1, NOW(), 1, NOW(), 0)
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), role_key = VALUES(role_key), status = '0', del_flag = 0;

INSERT INTO t_menu (id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, del_flag) VALUES
(1, '内容管理', 0, 1, 'content', NULL, 'M', '0', '0', NULL, 'document', 1, NOW(), 0),
(2, '系统管理', 0, 2, 'system', NULL, 'M', '0', '0', NULL, 'system', 1, NOW(), 0),
(101, '文章查询', 1, 1, '', NULL, 'F', '0', '0', 'content:article:list', '#', 1, NOW(), 0),
(102, '文章新增', 1, 2, '', NULL, 'F', '0', '0', 'content:article:add', '#', 1, NOW(), 0),
(103, '文章修改', 1, 3, '', NULL, 'F', '0', '0', 'content:article:edit', '#', 1, NOW(), 0),
(104, '文章删除', 1, 4, '', NULL, 'F', '0', '0', 'content:article:remove', '#', 1, NOW(), 0),
(111, '分类管理', 1, 5, '', NULL, 'F', '0', '0', 'content:category:manage', '#', 1, NOW(), 0),
(112, '标签管理', 1, 6, '', NULL, 'F', '0', '0', 'content:tag:manage', '#', 1, NOW(), 0),
(113, '友链管理', 1, 7, '', NULL, 'F', '0', '0', 'content:link:manage', '#', 1, NOW(), 0),
(201, '用户管理', 2, 1, '', NULL, 'F', '0', '0', 'system:user:manage', '#', 1, NOW(), 0),
(202, '角色管理', 2, 2, '', NULL, 'F', '0', '0', 'system:role:manage', '#', 1, NOW(), 0),
(203, '菜单管理', 2, 3, '', NULL, 'F', '0', '0', 'system:menu:manage', '#', 1, NOW(), 0)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), parent_id = VALUES(parent_id), perms = VALUES(perms), status = '0', del_flag = 0;

INSERT IGNORE INTO t_user_role (user_id, role_id) VALUES (1, 1);
INSERT IGNORE INTO t_role_menu (role_id, menu_id) SELECT 1, id FROM t_menu WHERE del_flag = 0;

DROP PROCEDURE add_column_if_missing;

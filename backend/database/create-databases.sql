-- 用 MySQL 管理员执行；不会清空已有表。
CREATE DATABASE IF NOT EXISTS quiz_homework CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS quiz_homework_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 应用账号和真实密码请按 config/application-local.properties 配置。
-- 数据表由 src/main/resources/schema-mysql.sql 在 Spring Boot 启动时创建。

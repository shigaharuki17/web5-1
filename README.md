# Quiz 后台管理

Vue 2 + Element UI + Vue Router 前端，配套 Java 17 / Spring Boot / MyBatis / MySQL 用户管理后端。

## 本地运行

后端位于 `backend`。本机已创建作业库 `quiz_homework`、独立测试库 `quiz_homework_test` 和应用账号。真实数据库密码保存在未上传的 `backend/config/application-local.properties`。

1. 前端根目录运行 `npm install`、`npm run build`。
2. 在 `backend` 目录运行 `mvn -s maven-settings.xml package`，然后执行 `java -jar target/quiz-backend-1.0.0.jar`。
3. 浏览器打开 http://127.0.0.1:8080/ ，页面和 API 由同一后端服务提供。

也可用 IDEA 打开 `backend/pom.xml`，选择 Java 17，将工作目录设为 `backend`，运行 `QuizApplication`。开发前端时运行 `npm run serve`，打开 http://127.0.0.1:7000/ ，`/api` 自动转发到 8080。

其他电脑参考 `backend/config/application-local.properties.example` 配置数据库。也可通过 `QUIZ_DB_URL`、`QUIZ_DB_USER`、`QUIZ_DB_PASSWORD` 环境变量配置。数据库需先创建，后端自动创建缺失的表，不清空现有用户。

## 功能

- 用户查询、新增、编辑、逻辑删除和物理删除由 MySQL 后端实际处理。
- 用户名为 4–20 位英文字母或数字，密码至少 8 位，必须二次确认；后端重新校验所有参数，并通过查询和唯一索引防止重复注册。
- 密码始终遮罩显示，服务端使用 BCrypt 单向加盐保存，API 不返回密码或密码派生值。
- 逻辑删除保留记录并隐藏于普通查询，物理删除无法恢复。
- 题目管理保留原有本地数据及全部操作。本次四份指导的数据库接口范围为用户管理。
- `backend/requests.http` 提供 Controller、注册、查询及删除接口示例。
- `mvn -s maven-settings.xml test` 使用真实 MySQL 独立测试库验证 MyBatis、主键回填、注册校验、密码保护、查重与删除。

## 静态演示版本

`npm run build:demo` 在 `dist-demo` 目录显式构建浏览器本地数据演示版，适用于 Vercel 静态托管；此模式的数据只保存在当前浏览器。现有线上静态页面使用此构建，本地数据库作业请访问 8080。

默认 `npm run build` 构建连接真实后端的版本。`VUE_APP_API_BASE_URL` 可指定实际 HTTPS 后端地址。静态托管服务无法直接运行 Java 后端。

## 目录

```text
src/views/element/       用户与题目页面
src/api/users.js        用户 API
backend/src/main/       Java 后端及 MySQL 初始化脚本
backend/src/test/       真实 MySQL 集成测试
backend/requests.http   接口示例
backend/config/         数据库本地配置，真实密码不上传
```

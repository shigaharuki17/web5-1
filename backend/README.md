# Quiz Java 后端

Java 17、Spring Boot 3.5、MyBatis 3、MySQL。采用 `Controller → Service → Mapper → MySQL` 三层结构，包名 `com.tfzhang.quiz`。

## 启动

1. 用 IDEA 打开 `backend/pom.xml`，项目 SDK 选择 Java 17，等待 Maven 导入。
2. 本机已创建 `quiz_homework` 库和独立的 `quiz_app` 应用账号；连接信息放在未提交的 `config/application-local.properties`。
3. 在 `backend` 目录运行 `mvn spring-boot:run`，或运行 `QuizApplication`。IDEA 运行配置的工作目录也设为 `backend`。
4. 打开 http://127.0.0.1:8080/hello 核对 `{ "code": 1, "msg": "success", "data": "Hello World" }`。

其他电脑可按 `config/application-local.properties.example` 配置。也可用 `QUIZ_DB_URL`、`QUIZ_DB_USER`、`QUIZ_DB_PASSWORD` 环境变量指定连接。程序只创建缺失的表，不会删除现有表或清空用户。数据库必须先创建，服务端默认只监听本机。

## 接口

所有接口返回 `{code, msg, data}`，`code=1` 成功，`code=0` 失败。参数校验同时在后端执行。接口示例见 `requests.http`，可直接在 IDEA HTTP Client 或 Apifox 中测试。

| 方法与路径 | 功能 |
| --- | --- |
| `GET /hello` | 无参数 Controller 测试 |
| `GET/POST /simpleParam` | 接收 name、age |
| `GET/POST /simpleUser` | 通过 SimpleUser 接收 name、age |
| `POST /register` | 接收 username、password、checkpassword，支持 JSON 和表单 |
| `GET /api/users?username=alice` | 查询未逻辑删除的用户，支持关键字 |
| `POST /api/users` | 与注册接口相同 |
| `PUT /api/users/{id}` | 编辑用户名；密码及确认密码都为空时保留原密码 |
| `DELETE /api/users/{id}` | 按 ID 逻辑删除 |
| `DELETE /api/users/by-name/{username}` | 按用户名逻辑删除 |
| `GET/POST /deleteById?id=1` | 兼容课程 ID 逻辑删除示例 |
| `GET/POST /deleteByName?username=alice` | 兼容课程用户名逻辑删除示例 |
| `DELETE /api/users/{id}/hard` | 按 ID 物理删除 |
| `DELETE /api/users/by-name/{username}/hard` | 物理删除该用户名全部记录 |
| `POST /hardDeleteById?id=1` | ID 物理删除兼容接口 |
| `POST /hardDeleteByName?username=alice` | 用户名物理删除兼容接口 |

用户名必须为 4–20 位英文字母或数字，活跃用户名不区分大小写且不能重复。密码为 8–64 位、UTF-8 编码不超过 72 字节，必须与确认密码一致。数据库保存 BCrypt 加盐派生值；实体序列化和日志输出均排除密码。新用户角色固定为普通用户，客户端不能指定管理员角色。

逻辑删除把 `isDelete` 置为 1，并更新 `updateTime`；普通查询不返回已删除用户。活跃用户名唯一索引防止并发重复注册，逻辑删除后允许再次使用该名称。物理删除使用 SQL DELETE，删除后无法恢复。

## MyBatis 测试

`mybatis_test_user` 独立表保存指导中的 Jone、Jack、Tom、Sandy、Billie 五条数据，避免其 name/age/email 结构与正式 user 表混淆。`MyBatisDemoMapper` 查询五条数据；`UserMapper.saveUser` 测试自增主键回填。

运行 `mvn test`。测试使用真实 MySQL 的 `quiz_homework_test` 独立库，事务测试自动回滚，不修改作业库用户；测试库也需要给应用账号授权。`schema-mysql.sql` 和 `data-mysql.sql` 在启动时自动初始化。

## 前后端一起运行

前端根目录运行 `npm run build`，然后在 `backend` 运行 `mvn package`。打出的 `target/quiz-backend-1.0.0.jar` 包含前端页面，运行 `java -jar target/quiz-backend-1.0.0.jar`，浏览器打开 http://127.0.0.1:8080/ 。

开发时也可分别运行后端和 `npm run serve`，开发服务器把 `/api` 转发到本机 8080。

题目管理沿用上一阶段的浏览器本地数据；本次四份指导的数据库接口范围为用户管理。

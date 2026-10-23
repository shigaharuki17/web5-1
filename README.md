# Quiz 后台管理

基于课程《2.2 前端项目开发》的 Vue 2 + Element UI 前端工程化作业。

## 运行

在项目目录打开终端：

```powershell
npm install
npm run serve
```

浏览器打开 http://127.0.0.1:7000/ ，默认进入用户管理。

首次使用需要联网安装依赖，后续执行 `npm run serve` 即可。

## 打包与预览

```powershell
npm run build
npm run preview
```

先停止开发服务器再运行预览，二者使用同一端口 7000。打包结果是 `dist` 文件夹，`publicPath` 使用相对路径，路由使用 hash 模式，适合静态部署。请通过 HTTP 服务器预览，不要双击 HTML 文件。若 7000 被占用，可停止原服务，或开发时执行 `npm run serve -- --port 7001`。

## 完成功能

- 共用的 Element Container 页头、侧边菜单、主内容布局。
- `/user` 用户管理：用户名查询、清空和重置、表格、日期、用户名标签、密码始终遮罩显示、新增、编辑、确认删除、真实分页。
- `/question` 题目管理：题目关键词查询、四选一题目表格、新增、编辑、确认删除、真实分页。
- Element Form 校验：用户名称长度、重复用户、密码长度、确认密码一致性、日期必填、题目及四个选项必填、选项去重、正确答案必选。
- 新增或修改后显示成功消息；删除最后一页最后一项后自动回到有效页码。
- 当前浏览器 localStorage 保存演示数据，刷新或切换路由后数据保留。
- 小窗口时表格可横向滚动，弹窗宽度随窗口适配。

这是 2.2 章节的独立前端练习，没有连接 Java 后端，没有真实账户登录。密码输入始终遮罩显示，浏览器只保存加盐派生值；旧版明文数据会在加载用户页时迁移。此项目仅用于课程演示。初始数据共 16 名用户和 12 道题；修改仅影响当前浏览器和当前网站地址。开发端口和预览端口一致时共享数据。

## 目录

```text
public/index.html                  网页入口
src/main.js                        注册 Element UI
src/App.vue                        共用后台布局与 router-view
src/router/index.js                /user、/question 路由
src/views/element/UserView.vue      用户管理组件
src/views/element/QuestionView.vue  题目管理组件
src/data.js                        示例数据及本地存储
vue.config.js                      相对资源路径、开发端口
preview.js                         打包结果预览服务
package-lock.json                  固定依赖
```

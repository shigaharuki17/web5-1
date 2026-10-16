# Quiz 后台管理

基于课程《2.2 前端项目开发》的 Vue 2 + Element UI 前端工程化作业。

## 运行

在项目目录打开终端：

```powershell
npm install
npm run serve
```

浏览器打开 http://127.0.0.1:7000/ ，默认进入用户管理。

也可以双击 `启动开发环境.cmd`。首次使用需要联网安装依赖，后续直接运行即可。

## 打包与预览

```powershell
npm run build
npm run preview
```

先停止开发服务器再运行预览，二者使用同一端口 7000。打包结果是 `dist` 文件夹，`publicPath` 使用相对路径，路由使用 hash 模式，适合静态部署。请通过 HTTP 服务器预览，不要双击 HTML 文件。若 7000 被占用，可停止原服务，或开发时执行 `npm run serve -- --port 7001`。

## 完成功能

- 共用的 Element Container 页头、侧边菜单、主内容布局。
- `/user` 用户管理：用户名查询、清空和重置、表格、日期、用户名标签、密码显示/隐藏、新增、编辑、确认删除、真实分页。
- `/question` 题目管理：题目关键词查询、四选一题目表格、新增、编辑、确认删除、真实分页。
- Element Form 校验：用户名称长度、重复用户、密码长度、日期必填、题目及四个选项必填、选项去重、正确答案必选。
- 新增或修改后显示成功消息；删除最后一页最后一项后自动回到有效页码。
- 当前浏览器 localStorage 保存演示数据，刷新或切换路由后数据保留。
- 小窗口时表格可横向滚动，弹窗宽度随窗口适配。

这是 2.2 章节的独立前端练习，没有连接 Java 后端，没有真实账户登录。演示密码仅为练习数据，不要填写真实密码。初始数据共 16 名用户和 12 道题；修改仅影响当前浏览器和当前网站地址。开发端口和预览端口一致时共享数据。

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

## 提交材料与录制建议

1. 源码：保留 `src`、`public`、配置文件、`package.json`、`package-lock.json`；不提交 `node_modules` 和安装缓存。
2. 打包结果：运行 `npm run build`，提供完整 `dist` 目录。
3. 演示：启动项目，录制用户页、题目页切换、查询、添加弹窗、编辑、删除确认与分页；录制时可补充展示组件目录、路由文件及打包成功结果。
4. 提交文件名中的姓名、学号，以及平台提交入口，请按老师的实际要求填写。项目不会代替你向学校平台提交。

## 参考材料

- 本地指导文本：`D:\webdesign-master\2.2前端项目开发.md`
- 本地提交要求视频：`vue-element作业提交要求.mp4`
- [教学视频一](https://www.bilibili.com/video/BV18rhez3Epx)
- [教学视频二](https://www.bilibili.com/video/BV18rhez3E5s)
- [教学视频三](https://www.bilibili.com/video/BV18rhez3EGo)
- [Vue 2 文档](https://v2.cn.vuejs.org/)
- [Element UI 文档](https://element.eleme.cn/#/zh-CN)

核对范围：已读取指导文本，并抽取本地提交视频多个时间点画面核对两页布局、组件、打包与演示形式。B 站页面及接口访问受限，未逐段观看三个在线教学视频；本地视频语音未自动转写，因此仅口头说明的提交细节还需你最终核对。

<template>
  <el-container class="app-shell">
    <el-header height="76px" class="app-header"><span>Quiz后台管理</span><span class="header-note">系统信息管理</span></el-header>
    <el-container>
      <el-aside width="200px" class="app-aside">
        <el-menu router :default-active="$route.path.startsWith('/question') ? '/question' : $route.path" :default-openeds="['system']">
          <el-submenu index="system">
            <template slot="title"><i class="el-icon-message"></i><span>系统信息管理</span></template>
            <el-menu-item index="/user"><i class="el-icon-user"></i>用户管理</el-menu-item>
            <el-menu-item index="/question"><i class="el-icon-document"></i>题目管理</el-menu-item>
          </el-submenu>
        </el-menu>
      </el-aside>
      <el-main><router-view></router-view></el-main>
    </el-container>
    <el-footer height="42px">Vue 2 · Element UI · Quiz 课程项目</el-footer>
  </el-container>
</template>

<script>
export default { name: 'App' }
</script>

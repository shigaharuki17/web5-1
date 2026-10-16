<template>
  <section>
    <div class="page-heading"><h1>用户管理</h1><span>维护用户信息，支持查询、新增与编辑</span></div>
    <el-form :inline="true" class="filter-form" @submit.native.prevent="search">
      <el-form-item label="用户名"><el-input v-model="keyword" placeholder="请输入用户名" clearable @clear="search" aria-label="查询用户名"></el-input></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" native-type="submit">查询用户</el-button><el-button type="success" icon="el-icon-plus" @click="openDialog()">添加用户</el-button><el-button @click="reset">重置</el-button></el-form-item>
    </el-form>
    <el-table :data="pageRows" row-key="id" stripe empty-text="没有符合条件的用户" style="width:100%">
      <el-table-column label="日期" prop="date" min-width="150"><template slot-scope="scope"><i class="el-icon-time"></i> {{ scope.row.date }}</template></el-table-column>
      <el-table-column label="用户名" prop="username" min-width="140"><template slot-scope="scope"><el-tag size="medium">{{ scope.row.username }}</el-tag></template></el-table-column>
      <el-table-column label="密码" min-width="140"><template slot-scope="scope"><span>{{ visibleIds.includes(scope.row.id) ? scope.row.password : '••••••' }}</span><el-button type="text" :aria-label="visibleIds.includes(scope.row.id) ? '隐藏密码' : '显示密码'" :icon="visibleIds.includes(scope.row.id) ? 'el-icon-lock' : 'el-icon-view'" @click="togglePassword(scope.row.id)"></el-button></template></el-table-column>
      <el-table-column label="操作" width="192"><template slot-scope="scope"><el-button size="mini" @click="openDialog(scope.row)">编辑</el-button><el-button size="mini" type="danger" @click="remove(scope.row)">删除</el-button></template></el-table-column>
    </el-table>
    <div class="pagination-row"><span class="record-note">用户信息 · 共 {{ filteredRows.length }} 条</span><el-pagination background :current-page="page" :page-size="pageSize" :page-sizes="[5,10,20]" :total="filteredRows.length" layout="total, sizes, prev, pager, next, jumper" @current-change="page=$event" @size-change="changeSize"></el-pagination></div>
    <el-dialog :title="editingId === null ? '添加用户' : '编辑用户'" :visible.sync="dialogVisible" width="500px" :close-on-click-modal="false" @closed="clearValidation">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px" class="dialog-form" @submit.native.prevent="submit">
        <el-form-item label="用户名" prop="username"><el-input v-model.trim="form.username" maxlength="20" show-word-limit placeholder="请输入用户名" autocomplete="off"></el-input></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" show-password maxlength="32" placeholder="请输入 6–32 位密码" autocomplete="new-password"></el-input></el-form-item>
        <el-form-item label="日期" prop="date"><el-date-picker v-model="form.date" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width:100%" :editable="false"></el-date-picker></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submit">确定</el-button></span>
    </el-dialog>
  </section>
</template>

<script>
import { initialUsers, loadData, saveData, nextId, today } from '../../data'
const KEY = 'quiz-admin-users-v1'
export default {
  name: 'UserView',
  data() {
    return {
      rows: loadData(KEY, initialUsers), keyword: '', appliedKeyword: '', page: 1, pageSize: 5,
      visibleIds: [], dialogVisible: false, editingId: null, form: { username: '', password: '', date: '' },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 2, max: 20, message: '用户名长度为 2–20 个字符', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 32, message: '密码长度为 6–32 位', trigger: 'blur' }, { validator: (_, value, callback) => callback(value && value.trim() ? undefined : new Error('密码不能全为空格')), trigger: 'blur' }],
        date: [{ required: true, message: '请选择日期', trigger: 'change' }]
      }
    }
  },
  computed: {
    filteredRows() { const k = this.appliedKeyword.toLowerCase(); return this.rows.filter(row => row.username.toLowerCase().includes(k)) },
    pageRows() { return this.filteredRows.slice((this.page - 1) * this.pageSize, this.page * this.pageSize) }
  },
  methods: {
    search() { this.appliedKeyword = this.keyword.trim(); this.page = 1 },
    reset() { this.keyword = ''; this.search() },
    changeSize(size) { this.pageSize = size; this.page = 1 },
    togglePassword(id) { this.visibleIds = this.visibleIds.includes(id) ? this.visibleIds.filter(v => v !== id) : [...this.visibleIds, id] },
    clearValidation() { if (this.$refs.form) this.$refs.form.clearValidate() },
    openDialog(row) {
      this.editingId = row ? row.id : null
      this.form = row ? { username: row.username, password: row.password, date: row.date } : { username: '', password: '', date: today() }
      this.dialogVisible = true
      this.$nextTick(this.clearValidation)
    },
    persist(nextRows) {
      try { saveData(KEY, nextRows); this.rows = nextRows; this.page = Math.max(1, Math.min(this.page, Math.ceil(this.filteredRows.length / this.pageSize))); return true }
      catch (_) { this.$message.error('保存失败，请检查浏览器是否允许本地存储'); return false }
    },
    submit() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        if (this.rows.some(row => row.username.toLowerCase() === this.form.username.toLowerCase() && row.id !== this.editingId)) return this.$message.warning('用户名已存在，请更换用户名')
        const record = { ...this.form, id: this.editingId === null ? nextId(this.rows) : this.editingId }
        const nextRows = this.editingId === null ? [record, ...this.rows] : this.rows.map(row => row.id === this.editingId ? record : row)
        if (this.persist(nextRows)) { this.dialogVisible = false; this.reset(); this.$message.success(this.editingId === null ? '添加用户成功' : '用户信息已更新') }
      })
    },
    async remove(row) {
      try { await this.$confirm(`确定删除用户“${row.username}”吗？`, '删除确认', { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }) }
      catch (_) { return }
      if (this.persist(this.rows.filter(item => item.id !== row.id))) this.$message.success('用户已删除')
    }
  }
}
</script>

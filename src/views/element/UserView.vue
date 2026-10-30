<template>
  <section v-loading="loading">
    <div class="page-heading"><h1>用户管理</h1><span>维护用户信息，支持查询、新增与编辑</span></div>
    <el-form :inline="true" class="filter-form" @submit.native.prevent="search">
      <el-form-item label="用户名"><el-input v-model="keyword" placeholder="请输入用户名" clearable @clear="search" aria-label="查询用户名"></el-input></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" native-type="submit">查询用户</el-button><el-button type="success" icon="el-icon-plus" :disabled="loading || !!storageError" @click="openDialog()">添加用户</el-button><el-button @click="reset">重置</el-button></el-form-item>
    </el-form>
    <el-alert v-if="storageError" :title="storageError" type="error" :closable="false" show-icon></el-alert>
    <el-button v-if="storageError" size="small" class="retry-users" @click="loadRows">重新连接</el-button>
    <el-table :data="pageRows" row-key="id" stripe empty-text="没有符合条件的用户" style="width:100%">
      <el-table-column label="日期" prop="date" min-width="150"><template slot-scope="scope"><i class="el-icon-time"></i> {{ scope.row.date }}</template></el-table-column>
      <el-table-column label="用户名" prop="username" min-width="140"><template slot-scope="scope"><el-tag size="medium">{{ scope.row.username }}</el-tag></template></el-table-column>
      <el-table-column label="密码" min-width="140"><template><span aria-label="密码已隐藏">••••••••</span></template></el-table-column>
      <el-table-column label="操作" :width="useBackend ? 280 : 192"><template slot-scope="scope"><el-button size="mini" @click="openDialog(scope.row)">编辑</el-button><el-button size="mini" type="danger" @click="remove(scope.row)">删除</el-button><el-button v-if="useBackend" size="mini" type="warning" plain @click="remove(scope.row, true)">物理删除</el-button></template></el-table-column>
    </el-table>
    <div class="pagination-row"><span class="record-note">用户信息 · 共 {{ filteredRows.length }} 条</span><el-pagination background :current-page="page" :page-size="pageSize" :page-sizes="[5,10,20]" :total="filteredRows.length" layout="total, sizes, prev, pager, next, jumper" @current-change="page=$event" @size-change="changeSize"></el-pagination></div>
    <el-dialog :title="editingId === null ? '添加用户' : '编辑用户'" :visible.sync="dialogVisible" width="520px" top="0" custom-class="management-dialog" append-to-body :close-on-click-modal="false" :close-on-press-escape="!saving" :show-close="!saving" @closed="onDialogClosed">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px" class="dialog-form" @submit.native.prevent="submit">
        <el-form-item label="用户名" prop="username"><el-input ref="username" v-model.trim="form.username" maxlength="20" show-word-limit :placeholder="useBackend ? '4–20 位英文字母或数字' : '请输入用户名'" autocomplete="off" :disabled="saving"></el-input></el-form-item>
        <el-form-item :label="editingId === null ? '密码' : '新密码'" prop="password" :required="editingId === null"><el-input v-model="form.password" type="password" maxlength="64" :placeholder="editingId === null ? '请输入 8–64 位密码' : '不修改密码请留空'" autocomplete="new-password" :disabled="saving"></el-input></el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword" :required="editingId === null || !!form.password"><el-input v-model="form.confirmPassword" type="password" maxlength="64" :placeholder="editingId === null ? '请再次输入密码' : '修改密码时请再次输入'" autocomplete="new-password" :disabled="saving" @keyup.enter.native="submit"></el-input></el-form-item>
        <el-form-item v-if="!useBackend" label="日期" prop="date"><el-date-picker v-model="form.date" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width:100%" :editable="false" :disabled="saving"></el-date-picker></el-form-item>
      </el-form>
      <span slot="footer"><el-button :disabled="saving" @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="saving" @click="submit">确定</el-button></span>
    </el-dialog>
  </section>
</template>

<script>
import { initialUsers, saveData, nextId, today } from '../../data'
import { hashPassword, loadUserData } from '../../passwords'
import { useBackend, fetchUsers, createUser, updateUser, deleteUser } from '../../api/users'
const KEY = 'quiz-admin-users-v1'
export default {
  name: 'UserView',
  data() {
    return {
      rows: [], useBackend, loadVersion: 0, loading: true, saving: false, storageError: '', keyword: '', appliedKeyword: '', page: 1, pageSize: 5,
      dialogVisible: false, editingId: null, form: { username: '', password: '', confirmPassword: '', date: '' },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: useBackend ? 4 : 2, max: 20, message: useBackend ? '用户名长度为 4–20 个字符' : '用户名长度为 2–20 个字符', trigger: 'blur' }, { validator: (_, value, callback) => callback(useBackend && value && !/^[a-zA-Z0-9]+$/.test(value) ? new Error('用户名只能包含英文字母和数字') : undefined), trigger: 'blur' }],
        password: [{ validator: (_, value, callback) => {
          if (!value && this.editingId !== null && !this.form.confirmPassword) return callback()
          if (!value) return callback(new Error('请输入密码'))
          if (!value.trim()) return callback(new Error('密码不能全为空格'))
          if (value.length < 8 || value.length > 64) return callback(new Error('密码长度为 8–64 位'))
          if (useBackend && new TextEncoder().encode(value).length > 72) return callback(new Error('密码的 UTF-8 编码不能超过 72 字节'))
          callback()
        }, trigger: 'blur' }],
        confirmPassword: [{ validator: (_, value, callback) => {
          if (!value && this.editingId !== null && !this.form.password) return callback()
          if (!value) return callback(new Error('请再次输入密码'))
          if (value !== this.form.password) return callback(new Error('两次输入的密码不一致'))
          callback()
        }, trigger: 'blur' }],
        date: [{ required: true, message: '请选择日期', trigger: 'change' }]
      }
    }
  },
  created() { this.loadRows() },
  watch: {
    'form.password'() {
      if (this.form.confirmPassword && this.$refs.form) this.$refs.form.validateField('confirmPassword')
    }
  },
  computed: {
    filteredRows() { const k = this.appliedKeyword.toLowerCase(); return this.rows.filter(row => row.username.toLowerCase().includes(k)) },
    pageRows() { return this.filteredRows.slice((this.page - 1) * this.pageSize, this.page * this.pageSize) }
  },
  methods: {
    async loadRows() {
      const version = ++this.loadVersion
      this.loading = true
      this.storageError = ''
      try {
        const rows = useBackend ? await fetchUsers(this.appliedKeyword) : await loadUserData(KEY, initialUsers)
        if (version === this.loadVersion) {
          this.rows = rows
          this.page = Math.max(1, Math.min(this.page, Math.ceil(this.filteredRows.length / this.pageSize)))
        }
      } catch (error) { if (version === this.loadVersion) this.storageError = error.message || '用户数据加载失败，请刷新重试' }
      finally { if (version === this.loadVersion) this.loading = false }
    },
    async search() { this.appliedKeyword = this.keyword.trim(); this.page = 1; if (useBackend) await this.loadRows() },
    reset() { this.keyword = ''; return this.search() },
    changeSize(size) { this.pageSize = size; this.page = 1 },
    clearValidation() { if (this.$refs.form) this.$refs.form.clearValidate() },
    onDialogClosed() { this.form.password = ''; this.form.confirmPassword = ''; this.clearValidation() },
    openDialog(row) {
      this.editingId = row ? row.id : null
      this.form = { username: row ? row.username : '', password: '', confirmPassword: '', date: row ? row.date : today() }
      this.dialogVisible = true
      this.$nextTick(() => { this.clearValidation(); if (this.$refs.username) this.$refs.username.focus() })
    },
    persist(nextRows) {
      try { saveData(KEY, nextRows); this.rows = nextRows; this.page = Math.max(1, Math.min(this.page, Math.ceil(this.filteredRows.length / this.pageSize))); return true }
      catch (_) { this.$message.error('保存失败，请检查浏览器是否允许本地存储'); return false }
    },
    submit() {
      if (this.saving || !this.$refs.form) return
      this.$refs.form.validate(async valid => {
        if (!valid || this.saving) return
        if (this.rows.some(row => row.username.toLowerCase() === this.form.username.toLowerCase() && row.id !== this.editingId)) return this.$message.warning('用户名已存在，请更换用户名')
        this.saving = true
        try {
          const creating = this.editingId === null
          if (useBackend) {
            if (creating) await createUser(this.form)
            else await updateUser(this.editingId, this.form)
            this.form.password = ''; this.form.confirmPassword = ''; this.dialogVisible = false
            await this.reset()
            this.$message.success(creating ? '添加用户成功' : '用户信息已更新')
            return
          }
          const existing = this.rows.find(row => row.id === this.editingId)
          const credentials = this.form.password ? await hashPassword(this.form.password) : { passwordHash: existing.passwordHash, passwordSalt: existing.passwordSalt, passwordIterations: existing.passwordIterations }
          const record = { username: this.form.username, date: this.form.date, ...credentials, id: creating ? nextId(this.rows) : this.editingId }
          const nextRows = creating ? [record, ...this.rows] : this.rows.map(row => row.id === this.editingId ? record : row)
          if (this.persist(nextRows)) { this.form.password = ''; this.form.confirmPassword = ''; this.dialogVisible = false; this.reset(); this.$message.success(creating ? '添加用户成功' : '用户信息已更新') }
        } catch (error) { this.$message.error(error.message || '密码保存失败，请重试') }
        finally { this.saving = false }
      })
    },
    async remove(row, hard = false) {
      try { await this.$confirm(hard ? `物理删除用户“${row.username}”后无法恢复，确定继续吗？` : `确定删除用户“${row.username}”吗？${useBackend ? '记录将保留在数据库中。' : ''}`, hard ? '物理删除确认' : '删除确认', { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }) }
      catch (_) { return }
      if (useBackend) {
        try { await deleteUser(row.id, hard); await this.loadRows(); this.$message.success(hard ? '用户记录已物理删除' : '用户已逻辑删除') }
        catch (error) { this.$message.error(error.message || '删除用户失败') }
        return
      }
      if (this.persist(this.rows.filter(item => item.id !== row.id))) this.$message.success('用户已删除')
    }
  }
}
</script>

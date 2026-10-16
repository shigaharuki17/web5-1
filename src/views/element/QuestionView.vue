<template>
  <section class="question-page">
    <div class="page-heading"><h1>题目管理</h1><span>维护选择题题库与正确答案</span></div>
    <el-form :inline="true" class="filter-form" @submit.native.prevent="search">
      <el-form-item label="题目"><el-input v-model="keyword" placeholder="请输入题目关键词" clearable @clear="search" aria-label="查询题目关键词"></el-input></el-form-item>
      <el-form-item><el-button type="success" class="question-search-button" icon="el-icon-search" native-type="submit">查询题目</el-button><el-button type="success" class="question-add-button" icon="el-icon-plus" @click="openDialog()">添加题目</el-button><el-button @click="reset">重置</el-button></el-form-item>
    </el-form>
    <el-table :data="pageRows" row-key="id" border stripe empty-text="没有符合条件的题目" style="width:100%">
      <el-table-column label="序号" type="index" :index="indexMethod" width="72"></el-table-column>
      <el-table-column label="问题" prop="title" min-width="210"></el-table-column>
      <el-table-column label="选项" min-width="200"><template slot-scope="scope"><ul class="options-list"><li v-for="(option,i) in scope.row.options" :key="i">{{ letters[i] }}. {{ option }}</li></ul></template></el-table-column>
      <el-table-column label="答案" min-width="140"><template slot-scope="scope"><el-tag type="success" size="small">{{ scope.row.answer }}</el-tag> {{ scope.row.options[letters.indexOf(scope.row.answer)] }}</template></el-table-column>
      <el-table-column label="操作" width="178"><template slot-scope="scope"><el-button size="mini" @click="openDialog(scope.row)">编辑</el-button><el-button size="mini" type="danger" @click="remove(scope.row)">删除</el-button></template></el-table-column>
    </el-table>
    <div class="pagination-row"><span class="record-note">选择题库 · 共 {{ filteredRows.length }} 道</span><el-pagination background :current-page="page" :page-size="pageSize" :page-sizes="[5,10,20]" :total="filteredRows.length" layout="total, sizes, prev, pager, next, jumper" @current-change="page=$event" @size-change="changeSize"></el-pagination></div>
    <el-dialog :title="editingId === null ? '添加题目' : '编辑题目'" :visible.sync="dialogVisible" width="620px" :close-on-click-modal="false" @closed="clearValidation">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px" class="dialog-form" @submit.native.prevent="submit">
        <el-form-item label="问题" prop="title"><el-input v-model.trim="form.title" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="请输入问题"></el-input></el-form-item>
        <el-form-item v-for="(letter,i) in letters" :key="letter" :label="'选项 '+letter" :prop="'options.'+i" :rules="optionRules"><el-input v-model.trim="form.options[i]" maxlength="100" :placeholder="'请输入选项 '+letter"></el-input></el-form-item>
        <el-form-item label="正确答案" prop="answer"><el-select v-model="form.answer" placeholder="请选择正确答案"><el-option v-for="letter in letters" :key="letter" :label="letter" :value="letter"></el-option></el-select></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submit">确定</el-button></span>
    </el-dialog>
  </section>
</template>

<script>
import { initialQuestions, loadData, saveData, nextId } from '../../data'
const KEY = 'quiz-admin-questions-v1'
export default {
  name: 'QuestionView',
  data() {
    return {
      rows: loadData(KEY, initialQuestions), keyword: '', appliedKeyword: '', page: 1, pageSize: 5, letters: ['A','B','C','D'],
      editDialogVisible: false, editingId: null, form: { title: '', options: ['','','',''], answer: '' },
      rules: { title: [{ required: true, message: '请输入问题', trigger: 'blur' }], answer: [{ required: true, message: '请选择正确答案', trigger: 'change' }] },
      optionRules: [{ required: true, message: '请输入此选项', trigger: 'blur' }]
    }
  },
  watch: {
    $route: {
      immediate: true,
      handler(route) {
        this.keyword = typeof route.query.keyword === 'string' ? route.query.keyword : ''
        this.appliedKeyword = this.keyword.trim()
        this.page = 1
        this.editDialogVisible = false
        this.editingId = null
        if (route.name === 'question-add') {
          this.form = { title: '', options: ['', '', '', ''], answer: '' }
          this.$nextTick(this.clearValidation)
        }
      }
    }
  },
  computed: {
    dialogVisible: {
      get() { return this.$route.name === 'question-add' || this.editDialogVisible },
      set(visible) {
        this.editDialogVisible = visible
        if (!visible && this.$route.name === 'question-add') {
          this.navigate({ name: 'question', query: this.$route.query }, true)
        }
      }
    },
    filteredRows() { const k = this.appliedKeyword.toLowerCase(); return this.rows.filter(row => row.title.toLowerCase().includes(k)) },
    pageRows() { return this.filteredRows.slice((this.page - 1) * this.pageSize, this.page * this.pageSize) }
  },
  methods: {
    indexMethod(index) { return (this.page - 1) * this.pageSize + index + 1 },
    navigate(location, replace = false) {
      if (this.$router.resolve(location).route.fullPath === this.$route.fullPath) return
      this.$router[replace ? 'replace' : 'push'](location).catch(error => {
        if (error.name !== 'NavigationDuplicated') this.$message.error('页面跳转失败，请重试')
      })
    },
    search() {
      this.appliedKeyword = this.keyword.trim()
      this.page = 1
      this.navigate({ name: 'question', query: this.appliedKeyword ? { keyword: this.appliedKeyword } : {} })
    },
    reset() { this.keyword = ''; this.search() },
    changeSize(size) { this.pageSize = size; this.page = 1 },
    clearValidation() { if (this.$refs.form) this.$refs.form.clearValidate() },
    openDialog(row) {
      if (!row) {
        this.navigate({ name: 'question-add', query: this.$route.query })
        return
      }
      this.editingId = row ? row.id : null
      this.form = row ? { title: row.title, options: [...row.options], answer: row.answer } : { title: '', options: ['','','',''], answer: '' }
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
        if (new Set(this.form.options.map(v => v.toLowerCase())).size !== 4) return this.$message.warning('四个选项不能重复')
        const creating = this.editingId === null
        const record = { title: this.form.title, options: [...this.form.options], answer: this.form.answer, id: this.editingId === null ? nextId(this.rows) : this.editingId }
        const nextRows = this.editingId === null ? [record, ...this.rows] : this.rows.map(row => row.id === this.editingId ? record : row)
        if (this.persist(nextRows)) { this.dialogVisible = false; this.reset(); this.$message.success(creating ? '添加题目成功' : '题目信息已更新') }
      })
    },
    async remove(row) {
      try { await this.$confirm(`确定删除题目“${row.title}”吗？`, '删除确认', { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }) }
      catch (_) { return }
      if (this.persist(this.rows.filter(item => item.id !== row.id))) this.$message.success('题目已删除')
    }
  }
}
</script>

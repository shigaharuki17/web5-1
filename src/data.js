// 本阶段是独立前端案例：演示数据只保存在当前浏览器，不需要后端服务。
export const initialUsers = Array.from({ length: 16 }, (_, i) => ({
  id: i + 1,
  date: `2025-10-${String(i + 1).padStart(2, '0')}`,
  username: ['王小虎', '张三', '李四', '赵六', '陈晨', '林晓', '刘洋', '周宁', '吴雨', '郑悦', '孙明', '何川', '许清', '高远', '宋佳', '唐可'][i],
  passwordHash: 'bf94cd21860e8a766f1eb254306b0aedfe9f62b9c08940bf00e19a76976c080f',
  passwordSalt: 'a87fb129507a51cbd3ce6af51eb60a45',
  passwordIterations: 100000
}))
export const initialQuestions = [
  ['法国的首都是哪里？', ['巴黎', '伦敦', '柏林', '罗马'], 'A'],
  ['Vue 中用于双向数据绑定的指令是？', ['v-if', 'v-for', 'v-model', 'v-once'], 'C'],
  ['HTML 中表示超链接的标签是？', ['div', 'a', 'span', 'p'], 'B'],
  ['CSS 中设置文字颜色的属性是？', ['color', 'margin', 'display', 'padding'], 'A'],
  ['JavaScript 中声明常量的关键字是？', ['var', 'let', 'const', 'static'], 'C'],
  ['Vue Router 中用于显示路由组件的是？', ['router-link', 'el-main', 'el-table', 'router-view'], 'D'],
  ['Element UI 的表格组件是？', ['el-form', 'el-table', 'el-menu', 'el-dialog'], 'B'],
  ['HTTP 状态码 200 通常表示什么？', ['请求成功', '未找到', '服务器错误', '无权限'], 'A'],
  ['数组中添加末尾元素的方法是？', ['pop', 'shift', 'push', 'slice'], 'C'],
  ['下列哪项是 Vue 的生命周期钩子？', ['created', 'color', 'href', 'select'], 'A'],
  ['Element UI 的分页组件是？', ['el-page', 'el-pagination', 'el-tabs', 'el-step'], 'B'],
  ['网页的结构主要由哪种语言描述？', ['CSS', 'SQL', 'HTML', 'Java'], 'C']
].map(([title, options, answer], i) => ({ id: i + 1, title, options, answer }))

export function loadData(key, fallback) {
  try {
    const stored = JSON.parse(localStorage.getItem(key))
    if (Array.isArray(stored) && stored.every(row => row && typeof row.id === 'number' && (key.includes('users') ? typeof row.username === 'string' && typeof row.passwordHash === 'string' && typeof row.date === 'string' : typeof row.title === 'string' && Array.isArray(row.options) && row.options.length === 4 && row.options.every(v => typeof v === 'string') && ['A','B','C','D'].includes(row.answer)))) return stored
  } catch (_) { /* 不可用或旧数据格式时使用演示数据。 */ }
  return JSON.parse(JSON.stringify(fallback))
}
export function saveData(key, rows) { localStorage.setItem(key, JSON.stringify(rows)) }
export const nextId = rows => Math.max(0, ...rows.map(row => row.id)) + 1
export function today() { const d = new Date(); return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}` }

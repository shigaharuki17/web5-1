const BASE = (process.env.VUE_APP_API_BASE_URL || '').replace(/\/$/, '')

async function request(path, method = 'GET', body) {
  const controller = new AbortController()
  const timeout = setTimeout(() => controller.abort(), 12000)
  try {
    const response = await fetch(`${BASE}/api/questions${path}`, {
      method, headers: body ? { 'Content-Type': 'application/json' } : {},
      body: body ? JSON.stringify(body) : undefined, signal: controller.signal
    })
    let result
    try { result = await response.json() } catch (_) { throw new Error('题目服务返回格式异常，请检查后端是否已启动') }
    if (!response.ok || result.code !== 1) throw new Error(result.msg || '题目操作失败')
    return result.data
  } catch (error) {
    if (error.name === 'AbortError') throw new Error('题目服务连接超时，请确认后端已启动')
    if (error instanceof TypeError) throw new Error('无法连接题目服务，请启动后端后重试')
    throw error
  } finally { clearTimeout(timeout) }
}
export async function fetchQuestions(keyword = '', page = 1, pageSize = 5) {
  const params = new URLSearchParams({ keyword, page: String(page), pageSize: String(pageSize) })
  const result = await request(`?${params}`)
  return { total: result.total, rows: result.qsBeanList.map(q => ({ id: q.id, title: q.questionText, options: q.options, answer: q.answerKey.toUpperCase() })) }
}
function payload(form) {
  return { question: form.title, optiona: form.options[0], optionb: form.options[1], optionc: form.options[2], optiond: form.options[3], answer: form.answer.toLowerCase() }
}
export const createQuestion = form => request('', 'POST', payload(form))
export const updateQuestion = (id, form) => request(`/${id}`, 'PUT', payload(form))
export const deleteQuestion = id => request(`/${id}`, 'DELETE')

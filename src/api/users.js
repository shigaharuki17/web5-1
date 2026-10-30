const BASE = (process.env.VUE_APP_API_BASE_URL || '').replace(/\/$/, '')
export const useBackend = process.env.VUE_APP_USER_DATA_SOURCE !== 'local'

async function request(path, method = 'GET', body) {
  const controller = new AbortController()
  const timeout = setTimeout(() => controller.abort(), 12000)
  try {
    const response = await fetch(`${BASE}/api/users${path}`, {
      method,
      headers: body ? { 'Content-Type': 'application/json' } : {},
      body: body ? JSON.stringify(body) : undefined,
      signal: controller.signal
    })
    let result
    try { result = await response.json() } catch (_) { throw new Error('用户服务返回格式异常，请检查后端是否已启动') }
    if (!response.ok || result.code !== 1) throw new Error(result.msg || '用户操作失败')
    return result.data
  } catch (error) {
    if (error.name === 'AbortError') throw new Error('用户服务连接超时，请确认后端已启动')
    if (error instanceof TypeError) throw new Error('无法连接用户服务，请启动后端后重试')
    throw error
  } finally { clearTimeout(timeout) }
}

export async function fetchUsers(keyword = '') {
  const users = await request(keyword ? `?username=${encodeURIComponent(keyword)}` : '')
  return users.map(user => ({ id: user.id, username: user.userName, date: (user.createTime || '').slice(0, 10), userRole: user.userRole }))
}
export const createUser = form => request('', 'POST', { username: form.username, password: form.password, checkpassword: form.confirmPassword })
export const updateUser = (id, form) => request(`/${id}`, 'PUT', { username: form.username, password: form.password, checkpassword: form.confirmPassword })
export const deleteUser = (id, hard = false) => request(`/${id}${hard ? '/hard' : ''}`, 'DELETE')

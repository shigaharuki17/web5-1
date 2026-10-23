// 仅保存加盐后的密码派生值；原始密码不进入用户列表或 localStorage。
const ITERATIONS = 100000
const hex = bytes => Array.from(bytes, value => value.toString(16).padStart(2, '0')).join('')

export async function hashPassword(password) {
  if (!window.crypto || !window.crypto.subtle) throw new Error('请通过 HTTPS 或 localhost 访问，以便安全保存密码')
  const salt = window.crypto.getRandomValues(new Uint8Array(16))
  const key = await window.crypto.subtle.importKey('raw', new TextEncoder().encode(password), 'PBKDF2', false, ['deriveBits'])
  const bits = await window.crypto.subtle.deriveBits({ name: 'PBKDF2', salt, iterations: ITERATIONS, hash: 'SHA-256' }, key, 256)
  return { passwordHash: hex(new Uint8Array(bits)), passwordSalt: hex(salt), passwordIterations: ITERATIONS }
}

export async function loadUserData(key, fallback) {
  let stored
  try { stored = JSON.parse(localStorage.getItem(key)) } catch (_) { stored = null }
  const validRows = Array.isArray(stored) && stored.every(row => row && Number.isFinite(row.id) && typeof row.username === 'string' && typeof row.date === 'string')
  const source = validRows ? stored : fallback
  const rows = []
  let migrated = false
  for (const row of source) {
    let credentials
    if (typeof row.password === 'string') {
      credentials = await hashPassword(row.password)
      migrated = true
    } else if (/^[a-f0-9]{64}$/.test(row.passwordHash) && /^[a-f0-9]{32}$/.test(row.passwordSalt) && row.passwordIterations >= ITERATIONS) {
      credentials = { passwordHash: row.passwordHash, passwordSalt: row.passwordSalt, passwordIterations: row.passwordIterations }
    } else {
      throw new Error('用户密码数据格式异常，请重新设置演示数据后再试')
    }
    rows.push({ id: row.id, username: row.username, date: row.date, ...credentials })
  }
  // 原位置覆盖旧版数据，保留已有用户并清除旧的明文密码字段。
  if (migrated) localStorage.setItem(key, JSON.stringify(rows))
  return rows
}

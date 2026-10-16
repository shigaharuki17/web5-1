// 零额外依赖的 dist 静态预览服务器。
const http = require('http')
const fs = require('fs')
const path = require('path')
const root = path.join(__dirname, 'dist')
const types = { '.html': 'text/html; charset=utf-8', '.js': 'text/javascript; charset=utf-8', '.css': 'text/css; charset=utf-8', '.woff': 'font/woff', '.ttf': 'font/ttf', '.png': 'image/png', '.ico': 'image/x-icon' }
http.createServer((req, res) => {
  let pathname
  try { pathname = decodeURIComponent(new URL(req.url, 'http://localhost').pathname) } catch (_) { res.writeHead(400); return res.end('Bad request') }
  const file = path.resolve(root, '.' + (pathname === '/' ? '/index.html' : pathname))
  if (!file.startsWith(root + path.sep)) { res.writeHead(403); return res.end('Forbidden') }
  fs.readFile(file, (error, content) => {
    if (error) { res.writeHead(404); return res.end('Not found. Run npm run build first.') }
    res.writeHead(200, { 'Content-Type': types[path.extname(file)] || 'application/octet-stream' }); res.end(content)
  })
}).listen(7000, '127.0.0.1', () => console.log('Quiz preview: http://127.0.0.1:7000/#/user')).on('error', error => { console.error(error.message); process.exit(1) })

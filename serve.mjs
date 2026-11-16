/* ============================================================================
   环游四海 V1.0 · 本地预览服务器（零依赖）
   ---------------------------------------------------------------------------
   为什么需要它：uni-app 的 H5 产物是 ES module，用 file:// 双击 index.html
   会被浏览器 CORS 拦掉脚本和样式，页面白屏。必须走 http:// 访问。

   用法：
     node serve.mjs              # 预览 dist/build/h5
     node serve.mjs --dev        # 预览 dist/dev/h5（npm run dev:h5 的产物目录）
     node serve.mjs --port 5200
   ========================================================================== */

import http from 'node:http'
import fs from 'node:fs'
import path from 'node:path'
import { exec } from 'node:child_process'
import { fileURLToPath } from 'node:url'

const ROOT_DIR = path.dirname(fileURLToPath(import.meta.url))
const args = process.argv.slice(2)
const argv = (k) => {
  const i = args.indexOf(k)
  return i >= 0 ? args[i + 1] : null
}

const target = args.includes('--dev')
  ? path.join(ROOT_DIR, 'dist', 'dev', 'h5')
  : path.join(ROOT_DIR, 'dist', 'build', 'h5')

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.mjs': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.webp': 'image/webp',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf',
  '.mp4': 'video/mp4',
  '.map': 'application/json; charset=utf-8'
}

if (!fs.existsSync(path.join(target, 'index.html'))) {
  console.error(`\n  [×] 找不到构建产物：${target}`)
  console.error('      先执行  npm run build:h5   再运行本脚本\n')
  process.exit(1)
}

const server = http.createServer((req, res) => {
  const urlPath = decodeURIComponent((req.url || '/').split('?')[0])
  let filePath = path.join(target, urlPath)

  // 目录 → index.html
  if (fs.existsSync(filePath) && fs.statSync(filePath).isDirectory()) {
    filePath = path.join(filePath, 'index.html')
  }
  // 不存在 → 只有「页面请求」（无扩展名或 .html）才回落 index.html；
  // 静态资源找不到就老老实实 404，避免把 HTML 当 JS 返回触发 MIME 报错
  if (!fs.existsSync(filePath)) {
    const ext = path.extname(filePath).toLowerCase()
    if (ext && ext !== '.html') {
      res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' })
      res.end('404 ' + urlPath)
      return
    }
    filePath = path.join(target, 'index.html')
  }

  fs.readFile(filePath, (err, buf) => {
    if (err) {
      res.writeHead(500, { 'Content-Type': 'text/plain; charset=utf-8' })
      res.end('500')
      return
    }
    const ext = path.extname(filePath).toLowerCase()
    // 兜底：若产物用相对路径（./assets/...），从 /pages/xxx/ 这类深层地址打开时
    // 浏览器会把 ./assets 解析成 /pages/xxx/assets → 404 → 又被回落成 index.html，
    // 于是脚本以 text/html 返回触发 MIME 报错白屏。这里统一改成根绝对路径。
    if (ext === '.html') {
      const fixed = buf
        .toString('utf8')
        .replace(/(href|src)="\.\//g, '$1="/')
      res.writeHead(200, {
        'Content-Type': 'text/html; charset=utf-8',
        'Cache-Control': 'no-cache'
      })
      res.end(fixed)
      return
    }
    res.writeHead(200, {
      'Content-Type': MIME[ext] || 'application/octet-stream',
      'Cache-Control': 'no-cache'
    })
    res.end(buf)
  })
})

function openBrowser(url) {
  const cmd =
    process.platform === 'win32' ? `start "" "${url}"`
      : process.platform === 'darwin' ? `open "${url}"`
        : `xdg-open "${url}"`
  exec(cmd, () => {})
}

let port = Number(argv('--port')) || 5181
let tries = 0

server.on('error', (e) => {
  if (e.code === 'EADDRINUSE' && !argv('--port') && tries < 20) {
    tries++
    port++
    server.listen(port, '127.0.0.1')
  } else {
    console.error(e.message)
    process.exit(1)
  }
})

server.on('listening', () => {
  const url = `http://127.0.0.1:${port}/`
  console.log('')
  console.log('  环游四海 V1.0 · 本地预览')
  console.log('  ' + '─'.repeat(46))
  console.log(`  浅色稿   ${url}`)
  console.log(`  夜间稿   ${url}?theme=dark`)
  console.log('  ' + '─'.repeat(46))
  console.log(`  目录     ${target}`)
  console.log('  停止     Ctrl + C')
  console.log('')
  if (!args.includes('--no-open')) openBrowser(url)
})

server.listen(port, '127.0.0.1')

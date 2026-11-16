import { chromium } from 'playwright-core'

const EDGE = 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe'
const base = 'http://127.0.0.1:5181/'
const theme = process.env.SHOT_THEME || 'light'
const only = process.env.SHOT_ONLY || ''

const pages = [
  ['/pages/index/index', '01-home'],
  ['/pages/route/detail?id=r-dali', '02-route'],
  ['/pages/mall/index', '03-mall'],
  ['/pages/order/index', '04-order'],
  ['/pages/mine/index', '05-mine'],
  ['/pages/share/index', '06-share'],
  ['/pages/wallet/income', '07-income'],
  ['/pages/wallet/recharge?tab=recharge', '08-recharge'],
  ['/pages/wallet/recharge?tab=transfer', '09-transfer'],
  ['/pages/wallet/withdraw', '10-withdraw'],
  ['/pages/commission/index', '11-commission'],
  ['/pages/auth/register', '12-register'],
  ['/pages/poster/index', '13-poster'],
  ['/pages/vip/index', '14-vip'],
  ['/pages/placeholder/index?name=%E7%BB%BF%E8%89%B2%E7%A7%AF%E5%88%86', '15-placeholder'],
  ['/pages/album/index', '16-album'],
  ['/pages/album/detail?id=a-2026-spring', '17-album-detail'],
  ['/pages/admin/index', '18-admin'],
  ['/pages/product/detail?id=p-mushroom', '19-product'],
  ['/pages/order/detail?id=HY20261115001', '20-order-detail'],
  ['/pages/auth/login', '21-login']
]

const browser = await chromium.launch({ executablePath: EDGE, headless: true })
const ctx = await browser.newContext({
  viewport: { width: 390, height: 844 },
  deviceScaleFactor: 2,
  isMobile: true,
  hasTouch: true,
  colorScheme: theme
})
const page = await ctx.newPage()
const errs = []
page.on('pageerror', e => errs.push('PAGEERROR: ' + e.message))
page.on('console', m => { if (m.type() === 'error') errs.push('CONSOLE: ' + m.text()) })
page.on('requestfailed', r => errs.push('REQFAIL: ' + r.url()))

for (const [route, name] of pages) {
  if (only && !name.includes(only)) continue
  await page.goto(base + '#' + route, { waitUntil: 'networkidle' })
  await page.waitForTimeout(1100)
  await page.screenshot({ path: `shots/${name}${theme === 'dark' ? '-dark' : ''}.png`, fullPage: true })
  console.log('shot', name)
}
if (errs.length) console.log('---\n' + [...new Set(errs)].slice(0, 12).join('\n'))
await browser.close()

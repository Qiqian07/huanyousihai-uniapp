import { chromium } from 'playwright-core'
const EDGE = 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe'
const base = 'http://127.0.0.1:5181/'
const browser = await chromium.launch({ executablePath: EDGE, headless: true })
const ctx = await browser.newContext({ viewport: { width: 390, height: 844 }, deviceScaleFactor: 2, isMobile: true, hasTouch: true, colorScheme: 'light' })
const page = await ctx.newPage()
const log = []
page.on('pageerror', e => log.push('PAGEERROR ' + e.message))

// A. 提现：输入 1000，校验 60 / 940
await page.goto(base + '#/pages/wallet/withdraw', { waitUntil: 'networkidle' })
await page.waitForTimeout(600)
const input = page.locator('input').first()
await input.fill('1000')
await page.waitForTimeout(400)
const calc = await page.locator('.calc').innerText()
log.push('WITHDRAW_CALC: ' + calc.replace(/\n/g, ' | '))
await page.screenshot({ path: 'shots/f1-withdraw-1000.png', fullPage: true })

// B. 提现 5001 → 应被拒（余额 1280 会先报余额不足，改用后台充值页测 5000 上限）
await page.goto(base + '#/pages/admin/index', { waitUntil: 'networkidle' })
await page.waitForTimeout(500)
await page.getByText('充值', { exact: true }).first().click()
await page.waitForTimeout(400)
const inputs = page.locator('input')
await inputs.nth(0).fill('13800000304')
await inputs.nth(1).fill('5001')
await page.waitForTimeout(400)
const reBody = await page.locator('.pane').innerText()
log.push('RECHARGE_5001: ' + reBody.replace(/\n/g, ' | ').slice(0, 200))
await page.screenshot({ path: 'shots/f2-recharge-5001.png', fullPage: true })

// C. 注册空提交 → 字段级报错
await page.goto(base + '#/pages/auth/register', { waitUntil: 'networkidle' })
await page.waitForTimeout(500)
await page.getByText('注册', { exact: true }).last().click()
await page.waitForTimeout(600)
const regBody = await page.locator('.page').innerText()
log.push('REGISTER_ERR: ' + regBody.split('\n').filter(l => l.includes('请填写') || l.includes('请选择') || l.includes('请设置')).join(' / '))
await page.screenshot({ path: 'shots/f3-register-error.png', fullPage: true })

// D. 商城 → 产品详情 → 立即购买 → 订单中心
await page.goto(base + '#/pages/mall/index', { waitUntil: 'networkidle' })
await page.waitForTimeout(600)
await page.locator('.pcard').first().click()
await page.waitForTimeout(700)
await page.getByText('立即购买').first().click()
await page.waitForTimeout(1200)
log.push('AFTER_BUY_URL: ' + page.url())
const orders = await page.locator('.page').innerText()
log.push('ORDER_LIST: ' + orders.split('\n').slice(0, 12).join(' | '))
await page.screenshot({ path: 'shots/f4-order-after-buy.png', fullPage: true })

console.log(log.join('\n'))
await browser.close()

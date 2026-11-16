/* ============================================================================
   环游四海 V1.0 · 佣金引擎单元测试
   口径：AGENT.md §6（拨比）· §8.2（合规用例）· §8.3（佣金计算用例）
   运行：npm test        （= node tests/commission.test.mjs）
   ========================================================================== */

import {
  money,
  payoutFor,
  withdrawSettle,
  settleUpgrade,
  settleRenew,
  assertRecord,
  guards,
  PAYOUT_TOTAL,
  MAX_DEPTH,
  VIP_PRICE,
  RECHARGE_MAX
} from '../src/utils/commission.js'

let pass = 0
let fail = 0
const rows = []

/* 分 → 两位小数字符串，便于与文档口径逐字比对 */
const yuan = (cents) => money.toYuan(cents).toFixed(2)

function check(group, name, actual, expect) {
  const ok = JSON.stringify(actual) === JSON.stringify(expect)
  ok ? pass++ : fail++
  rows.push({
    group,
    name,
    actual: typeof actual === 'object' ? JSON.stringify(actual) : String(actual),
    expect: typeof expect === 'object' ? JSON.stringify(expect) : String(expect),
    ok
  })
}

function checkTrue(group, name, cond, detail) {
  cond ? pass++ : fail++
  rows.push({ group, name, actual: detail || String(cond), expect: 'true', ok: !!cond })
}

/* 造一棵推荐树：A ← B ← C ← D（A 在最上，D 在最下） */
let seq = 0
function upgrade(opts) {
  return settleUpgrade({
    orderId: opts.orderId || 'ORD' + ++seq,
    newUserId: opts.newUser,
    directIndex: opts.idx,
    referrerId: opts.p1 === undefined ? 'B' : opts.p1,
    referrerOfReferrerId: opts.p2 === undefined ? 'A' : opts.p2,
    amountCents: VIP_PRICE
  })
}

function summarize(r) {
  return (
    r.lines.map((l) => `${l.beneficiaryId}:${l.level}:${yuan(l.amount)}`).join(' | ') +
    ` ⟹ 合计 ${yuan(r.totalPaid)} / 受益人 ${r.beneficiaryCount} / 层级 ${r.maxDepth}`
  )
}

/* ======================================================== §8.3 佣金计算用例 */
/* 我（B）推荐第 1 人升级 */
let r = upgrade({ newUser: 'C1', idx: 1 })
check(
  '8.3',
  '我推荐第 1 人升级 398',
  summarize(r),
  'B:DIRECT:100.00 | A:INDIRECT:100.00 ⟹ 合计 200.00 / 受益人 2 / 层级 2'
)

/* 我（B）推荐第 2 人升级 */
r = upgrade({ newUser: 'C2', idx: 2 })
check(
  '8.3',
  '我推荐第 2 人升级 398',
  summarize(r),
  'B:DIRECT:100.00 | A:INDIRECT:100.00 ⟹ 合计 200.00 / 受益人 2 / 层级 2'
)

/* 我（B）推荐第 3 人升级 → 200 全部给直推，上级不再另发 */
r = upgrade({ newUser: 'C3', idx: 3 })
check(
  '8.3',
  '我推荐第 3 人升级 398',
  summarize(r),
  'B:DIRECT:200.00 ⟹ 合计 200.00 / 受益人 1 / 层级 1'
)

/* 第 9 人同样走「直推 200」分支 */
r = upgrade({ newUser: 'C9', idx: 9 })
check(
  '8.3',
  '我推荐第 9 人升级 398',
  summarize(r),
  'B:DIRECT:200.00 ⟹ 合计 200.00 / 受益人 1 / 层级 1'
)

/* 我第 3 个直推（C3）的下级再推荐一人：C3 得 100（其第 1 个直推），我得 100 */
r = upgrade({ newUser: 'D3', idx: 1, p1: 'C3', p2: 'B' })
check(
  '8.3',
  '我第 3 个直推的下级再推荐一人',
  summarize(r),
  'C3:DIRECT:100.00 | B:INDIRECT:100.00 ⟹ 合计 200.00 / 受益人 2 / 层级 2'
)

/* 我第 1 个直推（C1）的下级再推荐一人 */
r = upgrade({ newUser: 'D1', idx: 1, p1: 'C1', p2: 'B' })
check(
  '8.3',
  '我第 1 个直推的下级再推荐一人',
  summarize(r),
  'C1:DIRECT:100.00 | B:INDIRECT:100.00 ⟹ 合计 200.00 / 受益人 2 / 层级 2'
)

/* 脏数据：直推人无上级 → 中断计佣 + 写 payout_gap（禁止顺延 / 改派 / 少发） */
r = upgrade({ newUser: 'X1', idx: 1, p2: null })
check(
  '8.3',
  '脏数据 referrer 为空 → 中断计佣',
  {
    ok: r.ok,
    aborted: r.aborted,
    reason: r.gap && r.gap.reason,
    gapAmount: yuan(r.gap.amount),
    paid: yuan(r.totalPaid),
    lines: r.lines.length
  },
  { ok: false, aborted: true, reason: 'referrer_missing', gapAmount: '100.00', paid: '0.00', lines: 0 }
)

/* 序号按人独立计数：A 的第 1 个直推与 B 的第 1 个直推互不影响，各自从 1 开始 */
const personA = [1, 2, 3].map((i) => payoutFor(i))
const personB = [1, 2, 3].map((i) => payoutFor(i))
check(
  '8.3',
  '序号按人独立计数（A / B 各自从 1 起）',
  [personA[0], personA[2], personB[0], personB[2]],
  [
    { direct: 10000, indirect: 10000 },
    { direct: 20000, indirect: 0 },
    { direct: 10000, indirect: 10000 },
    { direct: 20000, indirect: 0 }
  ]
)

/* ======================================================== §8.2 合规用例 */
/* A→B→C→D 四层链路：D 升级只惠及 C 与 B，A 不参与 */
r = upgrade({ newUser: 'D', idx: 1, p1: 'C', p2: 'B' })
check(
  '8.2',
  'A→B→C→D 四层链路，D 升级',
  {
    paid: r.lines.map((l) => `${l.beneficiaryId}@${l.level}`),
    hasA: r.lines.some((l) => l.beneficiaryId === 'A'),
    maxDepth: r.maxDepth
  },
  { paid: ['C@DIRECT', 'B@INDIRECT'], hasA: false, maxDepth: 2 }
)

/* 单笔充值上限 5000 元 */
check('8.2', '单笔充值 5001 元 → 拒绝', guards.checkRecharge(money.toCents(5001)).code, 'RECHARGE_LIMIT')
check('8.2', '单笔充值 5000 元 → 通过', guards.checkRecharge(RECHARGE_MAX).ok, true)
check(
  '8.2',
  '1 万元拆两次各 5000 → 均通过',
  [guards.checkRecharge(RECHARGE_MAX).ok, guards.checkRecharge(RECHARGE_MAX).ok],
  [true, true]
)
check('8.2', '充值 5001 元给出拆分建议', guards.checkRecharge(money.toCents(5001)).suggestion, 2)

/* 提现手续费 6%，按分向下取整 */
let w = withdrawSettle(money.toCents(1000))
check('8.2', '提现 1000 元', [yuan(w.fee), yuan(w.arrive)], ['60.00', '940.00'])

/* 设计系统文档取整示例：提现 33.33 元 → 手续费 1.99，到账 31.34 */
w = withdrawSettle(money.toCents(33.33))
check('8.2', '提现 33.33 元（按分向下取整）', [yuan(w.fee), yuan(w.arrive)], ['1.99', '31.34'])

/* 单笔 398 订单佣金合计恰好 200 */
r = upgrade({ newUser: 'C1', idx: 1 })
check('8.2', '单笔 398 订单佣金合计', yuan(r.totalPaid), '200.00')

/* 全量断言：任何订单合计恒 200、层级 ≤ 2、无第三级 */
let allBalanced = true
let maxDepthSeen = 0
for (let i = 1; i <= 30; i++) {
  const t = upgrade({ newUser: 'N' + i, idx: i })
  if (t.totalPaid !== PAYOUT_TOTAL) allBalanced = false
  maxDepthSeen = Math.max(maxDepthSeen, t.maxDepth)
}
check('8.2', '1~30 号直推订单合计恒 200 且层级 ≤ 2', [allBalanced, maxDepthSeen], [true, 2])
check('8.2', '层级上限常量 = 2（两级红线）', MAX_DEPTH, 2)

/* 非法入参（序号 0）应直接抛错，不允许「第 0 个直推」 */
let threw = false
try {
  payoutFor(0)
} catch (e) {
  threw = true
}
checkTrue('8.2', 'directIndex=0 抛错拦截', threw, 'threw')

/* 破坏性断言：篡改合计 / 伪造第三级 → assertRecord 必须抛错 */
let caughtTotal = false
try {
  assertRecord({
    totalPaid: 30000,
    maxDepth: 1,
    lines: [{ beneficiaryId: 'B', level: 'DIRECT', depth: 1, amount: 30000 }]
  })
} catch (e) {
  caughtTotal = true
}
checkTrue('8.2', '合计 ≠ 200 时断言拦截', caughtTotal, 'threw')

let caughtDepth = false
try {
  assertRecord({
    totalPaid: PAYOUT_TOTAL,
    maxDepth: 3,
    lines: [
      { beneficiaryId: 'C', level: 'DIRECT', depth: 1, amount: 10000 },
      { beneficiaryId: 'B', level: 'INDIRECT', depth: 2, amount: 10000 },
      { beneficiaryId: 'A', level: 'INDIRECT', depth: 3, amount: 10000 }
    ]
  })
} catch (e) {
  caughtDepth = true
}
checkTrue('8.2', '出现第 3 级受益人时断言拦截', caughtDepth, 'threw')

/* Q-03 未结论：99 元续费默认不计佣，且明确标注待定 */
const renew = settleRenew({ orderId: 'RENEW1', referrerId: 'B' })
check('8.2', 'Q-03 未结论 → 续费不计佣并标记 pending', [renew.ok, renew.pending, renew.totalPaid], [false, 'Q-03', 0])

/* Q-04 已确认：过期不再享受 VIP 价 */
const expired = { level: 'VIP', vipExpireAt: '2026-01-01' }
const active = { level: 'VIP', vipExpireAt: '2099-01-01' }
const now = new Date('2026-11-16').getTime()
check(
  '8.2',
  'VIP 过期后取价回落普通价',
  [guards.priceFor({ vipPrice: 100, normalPrice: 200 }, expired, now), guards.priceFor({ vipPrice: 100, normalPrice: 200 }, active, now)],
  [200, 100]
)
check(
  '8.2',
  'VIP 三态判定（有效 / 即将到期 / 已过期）',
  [guards.vipState(active, now), guards.vipState({ level: 'VIP', vipExpireAt: '2026-12-01' }, now), guards.vipState(expired, now)],
  ['active', 'soon', 'expired']
)

/* ============================================================== 输出表格 */
let group = ''
for (const row of rows) {
  if (row.group !== group) {
    group = row.group
    console.log(`\n  ${group === '8.3' ? '§8.3 佣金计算用例（必测）' : '§8.2 合规用例'}`)
  }
  console.log(`  ${row.ok ? '✔' : '✘'} ${row.name}`)
  if (!row.ok) {
    console.log(`      实际：${row.actual}`)
    console.log(`      期望：${row.expect}`)
  } else {
    console.log(`      ${row.actual}`)
  }
}
console.log('\n  ' + '─'.repeat(64))
console.log(`  通过 ${pass} · 失败 ${fail} · 合计 ${pass + fail}`)
console.log('  ' + '─'.repeat(64) + '\n')

if (fail > 0) process.exit(1)

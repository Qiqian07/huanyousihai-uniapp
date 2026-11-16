/* ============================================================================
   环游四海 V1.0 · 前端状态与业务动作（uni-app · Vue3 reactive）
   数据全部为脱敏样例；金额以「分」存储。
   ========================================================================== */
import { reactive, computed } from 'vue'
import {
  users as seedUsers,
  routes as seedRoutes,
  products as seedProducts,
  vipProduct,
  orders as seedOrders,
  commissions as seedCommissions,
  wallet as seedWallet,
  albums as seedAlbums,
  regions,
  copy,
  CURRENT_USER_ID,
  IMAGES
} from './data.js'
import {
  money,
  settleUpgrade,
  withdrawSettle,
  guards,
  VIP_PRICE,
  RECHARGE_MAX
} from '../utils/commission.js'

const state = reactive({
  users: JSON.parse(JSON.stringify(seedUsers)),
  routes: seedRoutes,
  products: seedProducts,
  vipProduct,
  orders: JSON.parse(JSON.stringify(seedOrders)),
  commissions: JSON.parse(JSON.stringify(seedCommissions)),
  wallet: JSON.parse(JSON.stringify(seedWallet)),
  albums: seedAlbums,
  regions,
  copy,
  images: IMAGES,
  currentUserId: CURRENT_USER_ID,
  loggedIn: true,
  /* 分享来源（扫码注册时带推荐关系） */
  inviteFrom: '林岚',
  toast: ''
})

/* ------------------------------------------------------------------ 计算 */
const currentUser = computed(() => state.users.find((u) => u.id === state.currentUserId) || state.users[0])
const isVip = computed(() => guards.isVipActive(currentUser.value))
const vipState = computed(() => guards.vipState(currentUser.value))

function userById(id) {
  return state.users.find((u) => u.id === id) || null
}
function userByPhone(phone) {
  return state.users.find((u) => u.phone === phone) || null
}
function maskPhone(phone) {
  if (!phone || phone.length < 11) return phone || '—'
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

/* 当前用户在某线路 / 商品上的适用价 */
function priceFor(item) {
  return guards.priceFor(item, currentUser.value)
}

/* 团队统计：团队人数 = 全部后代；个人人数 = 仅直推 */
function teamStats(uid = state.currentUserId) {
  const all = state.users.filter((u) => !u.hidden)
  const direct = all.filter((u) => u.referrerId === uid)
  const children = (id) => all.filter((u) => u.referrerId === id)
  let team = 0
  const queue = [...direct]
  while (queue.length) {
    const cur = queue.shift()
    team += 1
    queue.push(...children(cur.id))
  }
  return { team, direct: direct.length }
}

function directList(uid = state.currentUserId) {
  return state.users
    .filter((u) => u.referrerId === uid && !u.hidden)
    .map((u) => {
      const st = teamStats(u.id)
      return {
        ...u,
        team: st.team,
        direct: st.direct,
        referrerName: userById(u.referrerId)?.name || '—'
      }
    })
}

function myOrders(status) {
  const list = state.orders.filter((o) => o.userId === state.currentUserId)
  if (!status || status === '全部订单') return list
  return list.filter((o) => o.status === status)
}

function myCommissions() {
  return state.commissions.filter((c) => c.beneficiaryId === state.currentUserId)
}

function myWallet() {
  return state.wallet.filter((w) => w.userId === state.currentUserId)
}

function orderCounts() {
  const base = { 待付款: 0, 待收货: 0, 售后单: 0, 全部订单: 0 }
  state.orders
    .filter((o) => o.userId === state.currentUserId)
    .forEach((o) => {
      base.全部订单 += 1
      if (base[o.status] != null) base[o.status] += 1
    })
  return base
}

/* ------------------------------------------------------------------ 工具 */
function toast(msg) {
  state.toast = msg
  uni.showToast({ title: msg, icon: 'none', duration: 1800 })
}

function nowStr() {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

function nextOrderId() {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `HY${d.getFullYear()}${p(d.getMonth() + 1)}${p(d.getDate())}${String(state.orders.length + 1).padStart(3, '0')}`
}

function addWallet(txn) {
  state.wallet.unshift({ id: 'W' + (state.wallet.length + 1).toString().padStart(3, '0'), userId: state.currentUserId, ...txn })
}

/* ------------------------------------------------------------------ 动作 */
/* 注册：5 项必填；推荐人码为佣金链路前置条件，缺失一律不允许注册 */
function register(form) {
  const errors = {}
  if (!form.referrerCode) errors.referrerCode = copy.referrerRequired
  if (!form.name) errors.name = '请填写名称'
  if (!form.phone) errors.phone = '请填写账号 / 手机号'
  else if (!/^1\d{10}$/.test(form.phone)) errors.phone = '手机号格式不正确'
  else if (userByPhone(form.phone)) errors.phone = '该手机号已注册'
  if (!form.region || form.region.length !== 3 || !form.region[2]) errors.region = '请选择省 / 市 / 区（需选到区级）'
  if (!form.password) errors.password = '请设置密码'

  let referrer = null
  if (form.referrerCode) {
    referrer = userByPhone(form.referrerCode)
    if (!referrer) errors.referrerCode = '推荐人不存在，请核对手机号'
  }
  if (Object.keys(errors).length) return { ok: false, errors }

  const id = 'u-n' + (state.users.length + 1)
  const user = {
    id,
    name: form.name,
    phone: form.phone,
    password: form.password,
    region: form.region,
    referrerId: referrer.id,
    level: '普通',
    vipExpireAt: '',
    balance: 0,
    greenPoints: 0,
    createdAt: nowStr().slice(0, 10),
    upgradedAt: ''
  }
  state.users.push(user)
  state.currentUserId = id
  state.loggedIn = true
  state.inviteFrom = referrer.name
  return { ok: true, user, referrer }
}

function login(phone, password) {
  const u = userByPhone(phone)
  if (!u) return { ok: false, message: '账号不存在' }
  if (u.password && password && u.password !== password) return { ok: false, message: '密码不正确' }
  state.currentUserId = u.id
  state.loggedIn = true
  return { ok: true, user: u }
}

function logout() {
  state.loggedIn = false
}

function switchUser(id) {
  const u = userById(id)
  if (u) {
    state.currentUserId = id
    state.loggedIn = true
  }
}

/* 充值：单笔 ≤ 5000（公司侧为会员充值入余额） */
function recharge(yuan) {
  const cents = money.toCents(yuan)
  const check = guards.checkRecharge(cents)
  if (!check.ok) return check
  currentUser.value.balance += cents
  addWallet({ type: '充值', amount: cents, counterparty: '分公司代充', status: '已入账', createdAt: nowStr() })
  return { ok: true, cents }
}

/* 互转：会员之间一对一转账，双方余额原子更新 */
function transfer(phone, yuan) {
  const cents = money.toCents(yuan)
  const check = guards.checkTransfer(cents, currentUser.value.balance)
  if (!check.ok) return check
  const target = userByPhone(phone)
  if (!target) return { ok: false, message: '未找到该会员账号' }
  if (target.id === state.currentUserId) return { ok: false, message: '不能转给自己' }
  currentUser.value.balance -= cents
  target.balance += cents
  addWallet({ type: '互转转出', amount: cents, counterparty: `${target.name} ${maskPhone(target.phone)}`, status: '已入账', createdAt: nowStr() })
  state.wallet.unshift({
    id: 'W' + (state.wallet.length + 1).toString().padStart(3, '0'),
    userId: target.id,
    type: '互转转入',
    amount: cents,
    counterparty: `${currentUser.value.name} ${maskPhone(currentUser.value.phone)}`,
    status: '已入账',
    createdAt: nowStr()
  })
  return { ok: true, cents, target }
}

/* 提现：6% 手续费，按分向下取整；需人工审核 */
function withdraw(yuan) {
  const cents = money.toCents(yuan)
  const check = guards.checkWithdraw(cents, currentUser.value.balance)
  if (!check.ok) return check
  const settle = withdrawSettle(cents)
  currentUser.value.balance -= cents
  addWallet({
    type: '提现',
    amount: cents,
    fee: settle.fee,
    counterparty: '工商银行 · 尾号 8823',
    status: '审核中',
    createdAt: nowStr()
  })
  return { ok: true, settle }
}

/* 下单：区分 VIP 价 / 普通价 */
function createOrder({ kind, itemId, title, spec, amount, thumb }) {
  const id = nextOrderId()
  const order = {
    id,
    userId: state.currentUserId,
    kind,
    itemId,
    title,
    spec,
    amount,
    status: '待付款',
    createdAt: nowStr(),
    thumb
  }
  state.orders.unshift(order)
  return order
}

function payOrder(id) {
  const o = state.orders.find((x) => x.id === id)
  if (!o) return { ok: false, message: '订单不存在' }
  if (o.status !== '待付款') return { ok: false, message: '订单状态不可变更' }
  if (currentUser.value.balance < o.amount) return { ok: false, code: 'INSUFFICIENT', message: '余额不足' }
  currentUser.value.balance -= o.amount
  o.status = o.kind === '线路' ? '待收货' : '待收货'
  addWallet({
    type: '消费',
    amount: -o.amount,
    counterparty: o.title,
    status: '已入账',
    createdAt: nowStr()
  })
  return { ok: true, order: o }
}

/* 升级 VIP：支付 398 → 自动升级 → 触发两级计佣 */
function upgradeVip() {
  const u = currentUser.value
  if (u.balance < VIP_PRICE) return { ok: false, code: 'INSUFFICIENT', message: '余额不足，可通过互转或充值后完成升级' }

  const p1 = userById(u.referrerId)
  /* COMPLIANCE: 注册强制绑定推荐人 → p1 必然存在；取空即数据异常 */
  const p2 = p1 ? userById(p1.referrerId) : null

  u.balance -= VIP_PRICE
  const year = new Date().getFullYear() + 1
  u.level = 'VIP'
  u.vipExpireAt = `${year}-${new Date().getMonth() + 1 < 10 ? '0' : ''}${new Date().getMonth() + 1}-${new Date().getDate() < 10 ? '0' : ''}${new Date().getDate()}`
  u.upgradedAt = nowStr().slice(0, 10)

  const order = createOrder({
    kind: '商品',
    itemId: 'p-vip',
    title: 'AI 智能相册网站（升级 VIP）',
    spec: '398 权益 · 自动升级 VIP 会员',
    amount: VIP_PRICE,
    thumb: IMAGES.poster
  })
  order.status = '已完成'

  /* 直推序号：该新会员是 p1 的第几个直推（按 p1 独立计数） */
  const directIndex = p1
    ? state.users.filter((x) => x.referrerId === p1.id && !x.hidden).findIndex((x) => x.id === u.id) + 1
    : 0

  const record = settleUpgrade({
    orderId: order.id,
    newUserId: u.id,
    directIndex,
    referrerId: p1 ? p1.id : null,
    referrerOfReferrerId: p2 ? p2.id : null
  })

  if (record.ok) {
    record.lines.forEach((line) => {
      state.commissions.unshift({
        id: 'CM' + (state.commissions.length + 1).toString().padStart(3, '0'),
        orderId: line.orderId,
        beneficiaryId: line.beneficiaryId,
        level: line.level,
        amount: line.amount,
        status: '已入账',
        fromUserId: u.id,
        createdAt: nowStr()
      })
      const ben = userById(line.beneficiaryId)
      if (ben) ben.balance += line.amount
      state.wallet.unshift({
        id: 'W' + (state.wallet.length + 1).toString().padStart(3, '0'),
        userId: line.beneficiaryId,
        type: line.level === 'DIRECT' ? '佣金 · 直推' : '佣金 · 间推',
        amount: line.amount,
        counterparty: `${u.name} 升级 398`,
        status: '已入账',
        createdAt: nowStr()
      })
    })
  }

  addWallet({
    type: '升级 VIP',
    amount: -VIP_PRICE,
    counterparty: 'AI 智能相册网站',
    status: '已入账',
    createdAt: nowStr()
  })

  return { ok: true, order, record }
}

/* 续费 99 元/年 */
function renewVip() {
  const u = currentUser.value
  if (u.balance < 9900) return { ok: false, code: 'INSUFFICIENT', message: '余额不足，可通过充值后完成续费' }
  u.balance -= 9900
  const d = u.vipExpireAt ? new Date(u.vipExpireAt.replace(/-/g, '/')) : new Date()
  d.setFullYear(d.getFullYear() + 1)
  const p = (n) => String(n).padStart(2, '0')
  u.vipExpireAt = `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
  addWallet({ type: '续费 VIP', amount: -9900, counterparty: 'VIP 续费 1 年', status: '已入账', createdAt: nowStr() })
  return { ok: true, expireAt: u.vipExpireAt }
}

/* 修改密码 */
function changePassword(oldPwd, newPwd) {
  const u = currentUser.value
  if (u.password && oldPwd !== u.password) return { ok: false, message: '原密码不正确' }
  if (!newPwd || newPwd.length < 6) return { ok: false, message: '新密码至少 6 位' }
  u.password = newPwd
  return { ok: true }
}

/* 找回密码（演示：手机号 + 新密码直接重置） */
function resetPassword(phone, newPwd) {
  const u = userByPhone(phone)
  if (!u) return { ok: false, message: '账号不存在' }
  if (!newPwd || newPwd.length < 6) return { ok: false, message: '新密码至少 6 位' }
  u.password = newPwd
  return { ok: true }
}

export default {
  state,
  currentUser,
  isVip,
  vipState,
  userById,
  userByPhone,
  maskPhone,
  priceFor,
  teamStats,
  directList,
  myOrders,
  myCommissions,
  myWallet,
  orderCounts,
  register,
  login,
  logout,
  switchUser,
  recharge,
  transfer,
  withdraw,
  createOrder,
  payOrder,
  upgradeVip,
  renewVip,
  changePassword,
  resetPassword,
  toast,
  money,
  RECHARGE_MAX,
  VIP_PRICE
}

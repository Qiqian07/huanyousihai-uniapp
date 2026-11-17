/* ============================================================================
   环游四海 V1.0 · 前端状态与业务动作（uni-app · Vue3 reactive）
   双模式：
   - API 模式（默认，对接 ruoyi-hys 后端）：动作走服务端，状态以服务端为准；
   - 演示模式（本地 mock）：无后端时离线演示，逻辑与数据全在前端。
   切换：localStorage / uni storage 写入 hys_api_mode = '0' 即演示模式。
   金额一律以「分」存储（COMPLIANCE）。
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
import {
  isLoggedIn,
  setToken
} from '../api/request.js'
import {
  apiRegister,
  apiLogin,
  apiResetPassword,
  apiProfile,
  apiTeam,
  apiWallet,
  apiMyCommissions,
  apiMyOrders,
  apiMyRecharges,
  apiChangePassword,
  apiRecharge,
  apiTransfer,
  apiWithdraw,
  apiCreateOrder,
  apiPayOrder,
  apiUpgradeVip,
  apiRenewVip,
  apiState
} from '../api/index.js'

/* API 模式开关：storage hys_api_mode 显式设为 '0' 时退回演示模式 */
export const API_MODE = uni.getStorageSync('hys_api_mode') !== '0'

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
  loggedIn: API_MODE ? isLoggedIn() : true,
  /* 分享来源（扫码注册时带推荐关系） */
  inviteFrom: '林岚',
  toast: '',
  /* API 模式下的服务端聚合数据 */
  apiTeam: { team: 0, direct: 0 },
  apiDirects: [],
  apiConfig: {}
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
  if (API_MODE) {
    if (uid === state.currentUserId) return state.apiTeam
    const d = state.apiDirects.find((u) => u.id === uid)
    return d ? { team: d.team, direct: d.direct } : { team: 0, direct: 0 }
  }
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
  if (API_MODE) {
    return state.apiDirects.filter((u) => u.referrerId === uid)
  }
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

function addWallet(txn) {
  state.wallet.unshift({ id: 'W' + (state.wallet.length + 1).toString().padStart(3, '0'), userId: state.currentUserId, ...txn })
}

/* ------------------------------------------------------------------ 服务端数据映射 */
const TXN_TYPE_LABEL = {
  RECHARGE: '充值',
  TRANSFER_OUT: '互转转出',
  TRANSFER_IN: '互转转入',
  WITHDRAW: '提现',
  CONSUME: '消费',
  COMMISSION_DIRECT: '佣金 · 直推',
  COMMISSION_INDIRECT: '佣金 · 间推',
  VIP_UPGRADE: '升级 VIP',
  VIP_RENEW: '续费 VIP',
  WITHDRAW_REFUND: '提现退回'
}
const COMMISSION_STATUS = { GRANTED: '已入账', PENDING: '待发放', FROZEN: '冻结' }
const ORDER_KIND = { ROUTE: '线路', PRODUCT: '商品', VIP_UPGRADE: '商品', VIP_RENEW: '商品' }

function mapMember(m) {
  if (!m) return null
  return {
    id: 'm' + m.memberId,
    name: m.name,
    phone: m.phone,
    region: (m.region || '').split('/').filter(Boolean),
    referrerId: m.referrerId ? 'm' + m.referrerId : null,
    level: m.level === 'VIP' ? 'VIP' : '普通',
    vipExpireAt: m.vipExpireAt || '',
    balance: m.balanceCents || 0,
    greenPoints: m.greenPoints || 0,
    createdAt: (m.createTime || '').slice(0, 10),
    upgradedAt: m.upgradedAt || '',
    vipState: m.vipState || 'none',
    team: m.teamCount || 0,
    direct: m.directCount || 0,
    referrerName: m.referrerName || '—',
    hidden: false
  }
}

function mapTxn(t) {
  return {
    id: 'W' + t.txnId,
    userId: 'm' + t.memberId,
    type: TXN_TYPE_LABEL[t.type] || t.type,
    amount: t.amountCents,
    fee: t.feeCents || 0,
    counterparty: t.counterparty,
    status: t.status,
    createdAt: t.createTime
  }
}

function mapOrder(o) {
  return {
    id: o.orderId,
    userId: 'm' + o.memberId,
    kind: ORDER_KIND[o.kind] || o.kind,
    itemId: o.itemId,
    title: o.title,
    spec: o.spec,
    amount: o.amountCents,
    status: o.status,
    thumb: o.thumb,
    createdAt: o.createTime
  }
}

function mapCommission(c) {
  return {
    id: 'CM' + c.commissionId,
    orderId: c.orderId,
    beneficiaryId: 'm' + c.beneficiaryId,
    fromUserId: 'm' + c.fromMemberId,
    fromName: c.fromMemberName,
    level: c.level,
    amount: c.amountCents,
    status: COMMISSION_STATUS[c.status] || c.status,
    createdAt: c.createTime
  }
}

function mapRoute(r) {
  const type = (r.title || '').includes('自驾') ? '自驾游' : '组团游'
  let itinerary = []
  try {
    itinerary = r.itinerary ? JSON.parse(r.itinerary) : []
  } catch (e) {
    itinerary = []
  }
  return {
    id: 'r' + r.routeId,
    type,
    title: r.title,
    subtitle: '',
    days: r.days || 1,
    nights: Math.max((r.days || 1) - 1, 0),
    cover: r.cover,
    normalPrice: r.normalPriceCents,
    vipPrice: r.vipPriceCents,
    departCity: r.gatherPlace || '',
    capacity: 0,
    sold: 0,
    rating: '5.0',
    gather: { time: r.gatherTime || '', place: r.gatherPlace || '' },
    tags: [],
    selfDrive: type === '自驾游',
    video: !!r.videoUrl,
    itinerary: (itinerary || []).map((d, i) => ({
      day: 'DAY ' + String(d.day || i + 1).padStart(2, '0'),
      title: d.title || '',
      meet: d.meet || (r.gatherTime || ''),
      copy: d.desc || d.copy || '',
      img: d.img || r.cover
    }))
  }
}

function mapProduct(p) {
  return {
    id: 'p' + p.productId,
    type: '特产',
    title: p.title,
    cover: p.cover,
    normalPrice: p.normalPriceCents,
    vipPrice: p.vipPriceCents,
    origin: '',
    sales: 0,
    spec: p.spec || '',
    desc: ''
  }
}

function mapAlbum(a) {
  let images = {}
  try {
    images = a.images ? JSON.parse(a.images) : {}
  } catch (e) {
    images = {}
  }
  /* 兼容两种存储格式：分类对象 {hotel,food,scenery}（种子/小程序端）与 URL 数组（后台批量录入） */
  const thumbs = Array.isArray(images)
    ? images.slice()
    : [].concat(images.hotel || [], images.food || [], images.scenery || [])
  return {
    id: 'a' + a.albumId,
    season: a.tripDate || '',
    route: a.batchTitle,
    photos: thumbs.length,
    videos: 0,
    cover: a.cover,
    thumbs
  }
}

/* API 模式：以服务端为准刷新全部状态 */
let refreshing = null
function refreshState() {
  if (refreshing) return refreshing
  refreshing = Promise.all([apiState(), apiWallet(), apiMyCommissions(), apiMyOrders(), apiMyRecharges()])
    .then(([st, wallet, cms, orders, recharges]) => {
      if (!st.ok) {
        if (st.code === 401) {
          state.loggedIn = false
        }
        return
      }
      const profile = st.data.profile || {}
      const me = mapMember(profile.member)
      state.users = [me].concat((st.data.directs || []).map(mapMember))
      state.currentUserId = me.id
      state.loggedIn = true
      state.inviteFrom = profile.referrerName || '—'
      state.apiTeam = st.data.team || { team: 0, direct: 0 }
      state.apiDirects = (st.data.directs || []).map(mapMember)
      state.apiConfig = st.data.config || {}
      state.routes = (st.data.routes || []).map(mapRoute)
      state.products = (st.data.products || []).map(mapProduct)
      state.albums = (st.data.albums || []).map(mapAlbum)
      state.wallet = (wallet.ok ? wallet.data : []).map(mapTxn)
      state.commissions = (cms.ok ? cms.data : []).map(mapCommission)
      state.orders = (orders.ok ? orders.data : []).map(mapOrder)
    })
    .finally(() => {
      refreshing = null
    })
  return refreshing
}

if (API_MODE && isLoggedIn()) {
  refreshState()
}

/* API 模式下的服务端错误 → 前端字段错误映射（注册表单用） */
function fieldErrors(message) {
  const errors = {}
  if (!message) return { form: '操作失败' }
  if (message.includes('推荐人')) errors.referrerCode = message
  else if (message.includes('手机号')) errors.phone = message
  else if (message.includes('名称')) errors.name = message
  else if (message.includes('密码')) errors.password = message
  else if (message.includes('省 / 市 / 区') || message.includes('区级')) errors.region = message
  else errors.form = message
  return errors
}

/* ------------------------------------------------------------------ 动作 */

/* 注册：推荐人码必填；成功后自动登录 */
async function register(form) {
  if (!API_MODE) {
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

  const r = await apiRegister(form)
  if (!r.ok) {
    return { ok: false, errors: fieldErrors(r.message), formError: r.message }
  }
  if (r.data && r.data.token) setToken(r.data.token)
  await refreshState()
  return { ok: true, user: r.data.member, referrer: { name: r.data.referrerName } }
}

async function login(phone, password) {
  if (!API_MODE) {
    const u = userByPhone(phone)
    if (!u) return { ok: false, message: '账号不存在' }
    if (u.password && password && u.password !== password) return { ok: false, message: '密码不正确' }
    state.currentUserId = u.id
    state.loggedIn = true
    return { ok: true, user: u }
  }
  const r = await apiLogin(phone, password)
  if (!r.ok) return { ok: false, message: r.message }
  await refreshState()
  return { ok: true, user: r.data.member }
}

async function logout() {
  if (API_MODE) setToken('')
  state.loggedIn = false
}

async function switchUser(id) {
  const u = userById(id)
  if (u) {
    state.currentUserId = id
    state.loggedIn = true
  }
}

/* 充值：单笔 ≤ 5000（公司侧审核后入余额） */
async function recharge(yuan) {
  if (!API_MODE) {
    const cents = money.toCents(yuan)
    const check = guards.checkRecharge(cents)
    if (!check.ok) return check
    currentUser.value.balance += cents
    addWallet({ type: '充值', amount: cents, counterparty: '分公司代充', status: '已入账', createdAt: nowStr() })
    return { ok: true, cents }
  }
  const r = await apiRecharge(yuan)
  if (!r.ok) return { ok: false, code: r.code, message: r.message, suggestion: r.data && r.data.suggestion }
  await refreshState()
  return { ok: true, cents: money.toCents(yuan), message: r.data && r.data.message }
}

/* 互转：会员之间一对一转账，双方余额原子更新 */
async function transfer(phone, yuan) {
  if (!API_MODE) {
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
  const r = await apiTransfer(phone, yuan)
  if (!r.ok) return { ok: false, message: r.message }
  await refreshState()
  return { ok: true, target: { name: r.data && r.data.target } }
}

/* 提现：6% 手续费，按分向下取整；需人工审核 */
async function withdraw(yuan) {
  if (!API_MODE) {
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
  const r = await apiWithdraw(yuan)
  if (!r.ok) return { ok: false, message: r.message }
  const d = r.data || {}
  await refreshState()
  return {
    ok: true,
    settle: {
      amount: d.amount,
      fee: d.fee,
      arrive: d.arrive,
      feeRate: 0.06,
      expr: money.fmt(d.amount) + ' × 6% = ' + money.fmt(d.fee)
    }
  }
}

/* 下单：区分 VIP 价 / 普通价（API 模式价格由服务端计算） */
async function createOrder({ kind, itemId, title, spec, amount, thumb }) {
  if (!API_MODE) {
    const id = `HY${nowStr().replace(/[-: ]/g, '').slice(0, 8)}${String(state.orders.length + 1).padStart(3, '0')}`
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
  const serverKind = kind === '线路' ? 'ROUTE' : 'PRODUCT'
  const serverItemId = Number(String(itemId).replace(/^[rp]/, ''))
  const r = await apiCreateOrder(serverKind, serverItemId)
  if (!r.ok) {
    toast(r.message)
    return null
  }
  await refreshState()
  return mapOrder(r.data.order)
}

async function payOrder(id) {
  if (!API_MODE) {
    const o = state.orders.find((x) => x.id === id)
    if (!o) return { ok: false, message: '订单不存在' }
    if (o.status !== '待付款') return { ok: false, message: '订单状态不可变更' }
    if (currentUser.value.balance < o.amount) return { ok: false, code: 'INSUFFICIENT', message: '余额不足' }
    currentUser.value.balance -= o.amount
    o.status = '待收货'
    addWallet({
      type: '消费',
      amount: -o.amount,
      counterparty: o.title,
      status: '已入账',
      createdAt: nowStr()
    })
    return { ok: true, order: o }
  }
  const r = await apiPayOrder(id)
  if (!r.ok) return { ok: false, message: r.message }
  await refreshState()
  return { ok: true, order: mapOrder(r.data.order) }
}

/* 升级 VIP：支付 398 → 自动升级 → 服务端两级计佣 */
async function upgradeVip() {
  if (!API_MODE) {
    const u = currentUser.value
    if (u.balance < VIP_PRICE) return { ok: false, code: 'INSUFFICIENT', message: '余额不足，可通过互转或充值后完成升级' }

    const p1 = userById(u.referrerId)
    /* COMPLIANCE: 注册强制绑定推荐人 → p1 必然存在；取空即数据异常 */
    const p2 = p1 ? userById(p1.referrerId) : null

    u.balance -= VIP_PRICE
    const year = new Date().getFullYear() + 1
    u.level = 'VIP'
    u.vipExpireAt = `${year}-${String(new Date().getMonth() + 1).padStart(2, '0')}-${String(new Date().getDate()).padStart(2, '0')}`
    u.upgradedAt = nowStr().slice(0, 10)

    const order = await createOrder({
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
  const r = await apiUpgradeVip()
  if (!r.ok) return { ok: false, code: 'INSUFFICIENT', message: r.message }
  await refreshState()
  const d = r.data || {}
  if (d.commission === 'interrupted') {
    toast(d.alert || '计佣已中断，请联系平台处理')
  }
  return { ok: true, order: mapOrder(d.order), alert: d.alert }
}

/* 续费 99 元/年 */
async function renewVip() {
  if (!API_MODE) {
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
  const r = await apiRenewVip()
  if (!r.ok) return { ok: false, code: 'INSUFFICIENT', message: r.message }
  await refreshState()
  return { ok: true, expireAt: r.data && r.data.expireAt }
}

/* 修改密码 */
async function changePassword(oldPwd, newPwd) {
  if (!API_MODE) {
    const u = currentUser.value
    if (u.password && oldPwd !== u.password) return { ok: false, message: '原密码不正确' }
    if (!newPwd || newPwd.length < 6) return { ok: false, message: '新密码至少 6 位' }
    u.password = newPwd
    return { ok: true }
  }
  const r = await apiChangePassword(oldPwd, newPwd)
  if (!r.ok) return { ok: false, message: r.message }
  return { ok: true }
}

/* 找回密码 */
async function resetPassword(phone, newPwd) {
  if (!API_MODE) {
    const u = userByPhone(phone)
    if (!u) return { ok: false, message: '账号不存在' }
    if (!newPwd || newPwd.length < 6) return { ok: false, message: '新密码至少 6 位' }
    u.password = newPwd
    return { ok: true }
  }
  const r = await apiResetPassword(phone, newPwd)
  if (!r.ok) return { ok: false, message: r.message }
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
  refreshState,
  toast,
  money,
  RECHARGE_MAX,
  VIP_PRICE
}

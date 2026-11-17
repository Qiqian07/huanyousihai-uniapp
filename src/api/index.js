/* ============================================================================
 * 环游四海 V1.0 · 后端接口定义（对应 ruoyi-hys 模块 /api/hys/**）
 * ========================================================================== */
import { request, setToken } from './request.js'

/* ---------------------------------------------------------------- 认证 */
export const apiRegister = (form) => request('/auth/register', { method: 'POST', data: form })

export function apiLogin(phone, password) {
  return request('/auth/login', { method: 'POST', data: { phone, password } }).then((r) => {
    if (r.ok && r.data && r.data.token) setToken(r.data.token)
    return r
  })
}

export const apiResetPassword = (phone, password) =>
  request('/auth/resetPassword', { method: 'POST', data: { phone, password } })

/* ---------------------------------------------------------------- 会员 */
export const apiProfile = () => request('/member/profile')
export const apiTeam = () => request('/member/team')
export const apiWallet = () => request('/member/wallet')
export const apiMyCommissions = () => request('/member/commissions')
export const apiMyOrders = () => request('/member/orders')
export const apiMyRecharges = () => request('/member/recharges')
export const apiChangePassword = (oldPassword, newPassword) =>
  request('/member/changePassword', { method: 'POST', data: { oldPassword, newPassword } })

/* ---------------------------------------------------------------- 钱包 */
export const apiRecharge = (amount) => request('/wallet/recharge', { method: 'POST', data: { amount } })
export const apiTransfer = (phone, amount) => request('/wallet/transfer', { method: 'POST', data: { phone, amount } })
export const apiWithdraw = (amount) => request('/wallet/withdraw', { method: 'POST', data: { amount } })

/* ---------------------------------------------------------------- 订单 */
export const apiCreateOrder = (kind, itemId) => request('/order/create', { method: 'POST', data: { kind, itemId } })
export const apiPayOrder = (orderId) => request('/order/pay', { method: 'POST', data: { orderId } })

/* ---------------------------------------------------------------- VIP */
export const apiUpgradeVip = () => request('/vip/upgrade', { method: 'POST' })
export const apiRenewVip = () => request('/vip/renew', { method: 'POST' })

/* ---------------------------------------------------------------- 目录/聚合状态 */
export const apiState = () => request('/catalog/state')

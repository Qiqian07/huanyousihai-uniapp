import request from '@/utils/request'

// ===== 会员 =====
export function listMember(query) {
  return request({ url: '/hys/member/list', method: 'get', params: query })
}

// ===== 充值审核 =====
export function listRecharge(query) {
  return request({ url: '/hys/recharge/list', method: 'get', params: query })
}
export function approveRecharge(rechargeId) {
  return request({ url: '/hys/recharge/approve/' + rechargeId, method: 'post' })
}
export function rejectRecharge(rechargeId, remark) {
  return request({ url: '/hys/recharge/reject/' + rechargeId, method: 'post', data: { remark } })
}

// ===== 提现审核 =====
export function listWithdraw(query) {
  return request({ url: '/hys/withdraw/list', method: 'get', params: query })
}
export function approveWithdraw(withdrawId) {
  return request({ url: '/hys/withdraw/approve/' + withdrawId, method: 'post' })
}
export function rejectWithdraw(withdrawId, remark) {
  return request({ url: '/hys/withdraw/reject/' + withdrawId, method: 'post', data: { remark } })
}

// ===== 线路上架 =====
export function listRoute(query) {
  return request({ url: '/hys/route/list', method: 'get', params: query })
}
export function addRoute(data) {
  return request({ url: '/hys/route', method: 'post', data })
}
export function updateRoute(data) {
  return request({ url: '/hys/route', method: 'put', data })
}
export function changeRouteStatus(routeId, status) {
  return request({ url: '/hys/route/status/' + routeId + '/' + status, method: 'post' })
}

// ===== 商品上架 =====
export function listProduct(query) {
  return request({ url: '/hys/product/list', method: 'get', params: query })
}
export function addProduct(data) {
  return request({ url: '/hys/product', method: 'post', data })
}
export function updateProduct(data) {
  return request({ url: '/hys/product', method: 'put', data })
}
export function changeProductStatus(productId, status) {
  return request({ url: '/hys/product/status/' + productId + '/' + status, method: 'post' })
}

// ===== 相册管理 =====
export function listAlbum(query) {
  return request({ url: '/hys/album/list', method: 'get', params: query })
}
export function addAlbum(data) {
  return request({ url: '/hys/album', method: 'post', data })
}
export function updateAlbum(data) {
  return request({ url: '/hys/album', method: 'put', data })
}

// ===== 异常对账 =====
export function listRecon(query) {
  return request({ url: '/hys/recon/list', method: 'get', params: query })
}
export function repairRecon(gapId, referrerPhone) {
  return request({ url: '/hys/recon/repair/' + gapId, method: 'post', data: { referrerPhone } })
}

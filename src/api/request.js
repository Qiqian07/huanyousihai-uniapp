/* ============================================================================
 * 环游四海 V1.0 · 后端 API 请求封装
 * 服务端权威口径：金额由后端换算与校验，客户端传金额一律忽略。
 * ========================================================================== */

/* 后端地址：H5 联调默认 localhost:8080；小程序真机需改为局域网 IP 或线上域名 */
export const API_BASE = 'http://localhost:8080/api/hys'

const TOKEN_KEY = 'hys_token'

export function getToken() {
  return uni.getStorageSync(TOKEN_KEY) || ''
}

export function setToken(token) {
  if (token) uni.setStorageSync(TOKEN_KEY, token)
  else uni.removeStorageSync(TOKEN_KEY)
}

export function isLoggedIn() {
  return !!getToken()
}

/**
 * 统一请求：返回 {ok, data, message}，HTTP 层错误统一转成 ok:false
 */
export function request(path, { method = 'GET', data = null } = {}) {
  return new Promise((resolve) => {
    uni.request({
      url: API_BASE + path,
      method,
      data,
      timeout: 10000,
      header: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + getToken()
      },
      success(res) {
        const body = res.data || {}
        if (res.statusCode === 401) {
          setToken('')
          resolve({ ok: false, code: 401, message: body.msg || '未登录或登录已过期' })
          return
        }
        /* RuoYi AjaxResult: code 200 成功 */
        if (res.statusCode === 200 && body.code === 200) {
          resolve({ ok: true, data: body.data, message: body.msg })
        } else {
          resolve({ ok: false, message: body.msg || ('请求失败 ' + res.statusCode) })
        }
      },
      fail(err) {
        resolve({ ok: false, message: '网络异常：' + (err.errMsg || '无法连接服务器') })
      }
    })
  })
}

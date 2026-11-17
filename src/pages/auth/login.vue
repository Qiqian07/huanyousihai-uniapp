<template>
  <view class="page">
    <hys-navbar title="登录" />

    <view class="wrap">
      <view class="brand">
        <text class="brand-en serif">HUANYOU SIHai</text>
        <text class="h1">环游四海</text>
        <text class="brand-sub">线上组织线路 · 线下执行游玩</text>
      </view>

      <hys-field label="账号 / 手机号" required :error="error">
        <input class="field num" v-model="phone" type="number" placeholder="请输入手机号" placeholder-class="field-ph" maxlength="11" />
      </hys-field>

      <hys-field label="密码" required>
        <input class="field" :password="!showPwd" v-model="password" placeholder="请输入密码" placeholder-class="field-ph" />
        <text class="eye" @tap="showPwd = !showPwd">{{ showPwd ? '隐藏' : '显示' }}</text>
      </hys-field>

      <view class="btn btn-primary mt8" @tap="submit">
        <text>登录</text>
      </view>

      <view class="links">
        <text class="link" @tap="go('/pages/auth/register')">没有账号，去注册</text>
        <text class="link" @tap="go('/pages/auth/forgot')">忘记密码</text>
      </view>

      <view class="demo card">
        <text class="demo-t">演示账号（脱敏样例）</text>
        <view v-for="u in demo" :key="u.id" class="demo-row" @tap="fill(u)">
          <text class="demo-n">{{ u.name }}</text>
          <text class="demo-p num">{{ u.phone }}</text>
          <text class="demo-l">{{ u.level }}</text>
        </view>
      </view>

      <hys-notice class="mt20" type="info" text="推荐关系为必填项；无邀请码无法注册" />
      <view class="bottom-pad" />
    </view>
  </view>
</template>

<script>
import store from '@/store/index.js'

export default {
  data() {
    return { phone: '', password: '', showPwd: false, error: '' }
  },
  computed: {
    demo() {
      return store.state.users.filter((u) => !u.hidden).slice(2, 5)
    }
  },
  methods: {
    fill(u) {
      this.phone = u.phone
      this.password = ''
    },
    async submit() {
      if (!this.phone) {
        this.error = '请填写账号 / 手机号'
        return
      }
      const r = await store.login(this.phone, this.password)
      if (!r.ok) {
        this.error = ''
        store.toast(r.message)
        return
      }
      uni.switchTab({ url: '/pages/index/index' })
    },
    go(url) {
      uni.navigateTo({ url })
    }
  }
}
</script>

<style scoped>
.brand {
  padding: 32rpx 0 40rpx;
}
.brand-en {
  display: block;
  font-size: 22rpx;
  letter-spacing: 4rpx;
  color: var(--c-vip-bronze);
  text-transform: uppercase;
}
.brand-sub {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: var(--c-slate);
}
.eye {
  display: block;
  margin-top: 10rpx;
  text-align: right;
  font-size: 22rpx;
  color: var(--c-slate);
}
.mt8 { margin-top: 8rpx; }
.links {
  display: flex;
  justify-content: space-between;
  padding: 28rpx 0;
}
.link {
  font-size: 24rpx;
  color: var(--c-slate);
}
.demo {
  padding: 20rpx 24rpx;
}
.demo-t {
  font-size: 22rpx;
  color: var(--c-slate);
}
.demo-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid var(--c-mist);
}
.demo-row:last-child { border-bottom: none; }
.demo-n {
  font-size: 26rpx;
  color: var(--c-ink);
}
.demo-p {
  font-size: 24rpx;
  color: var(--c-slate);
}
.demo-l {
  font-size: 22rpx;
  color: var(--c-vip-bronze);
}
.mt20 { margin-top: 20rpx; }
.bottom-pad { height: 60rpx; }
</style>

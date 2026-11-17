<template>
  <view class="page">
    <hys-navbar title="找回密码" back />

    <view class="wrap">
      <hys-field label="账号 / 手机号" required :error="error" help="账号即手机号">
        <input class="field num" v-model="phone" type="number" placeholder="请输入注册手机号" placeholder-class="field-ph" maxlength="11" />
      </hys-field>
      <hys-field label="新密码" required help="至少 6 位">
        <input class="field" password v-model="newPwd" placeholder="请设置新密码" placeholder-class="field-ph" />
      </hys-field>
      <hys-field label="确认新密码" required>
        <input class="field" password v-model="confirmPwd" placeholder="再次输入新密码" placeholder-class="field-ph" />
      </hys-field>

      <view class="btn btn-primary mt24" @tap="submit">
        <text>重置密码</text>
      </view>

      <hys-notice class="mt20" type="info" text="1.0 采用手机号 + 新密码重置流程；短信验证码能力后续接入" />
    </view>
  </view>
</template>

<script>
import store from '@/store/index.js'

export default {
  data() {
    return { phone: '', newPwd: '', confirmPwd: '', error: '' }
  },
  methods: {
    async submit() {
      if (!this.phone) {
        this.error = '请填写账号 / 手机号'
        return
      }
      if (this.newPwd !== this.confirmPwd) {
        store.toast('两次输入的新密码不一致')
        return
      }
      const r = await store.resetPassword(this.phone, this.newPwd)
      if (!r.ok) {
        this.error = r.message
        return
      }
      this.error = ''
      uni.showModal({
        title: '密码已重置',
        content: '请使用新密码登录',
        showCancel: false,
        success: () => uni.navigateTo({ url: '/pages/auth/login' })
      })
    }
  }
}
</script>

<style scoped>
.mt24 { margin-top: 24rpx; }
.mt20 { margin-top: 20rpx; }
</style>

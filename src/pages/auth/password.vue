<template>
  <view class="page">
    <hys-navbar title="修改密码" back />

    <view class="wrap">
      <hys-field label="原密码" required :error="error">
        <input class="field" password v-model="oldPwd" placeholder="请输入原密码" placeholder-class="field-ph" />
      </hys-field>
      <hys-field label="新密码" required help="至少 6 位">
        <input class="field" password v-model="newPwd" placeholder="请设置新密码" placeholder-class="field-ph" />
      </hys-field>
      <hys-field label="确认新密码" required>
        <input class="field" password v-model="confirmPwd" placeholder="再次输入新密码" placeholder-class="field-ph" />
      </hys-field>

      <view class="btn btn-primary mt24" @tap="submit">
        <text>确认修改</text>
      </view>

      <hys-notice class="mt20" type="info" text="修改后请使用新密码登录；推荐关系与团队位置不受影响" />
    </view>
  </view>
</template>

<script>
import store from '@/store/index.js'

export default {
  data() {
    return { oldPwd: '', newPwd: '', confirmPwd: '', error: '' }
  },
  methods: {
    submit() {
      if (this.newPwd !== this.confirmPwd) {
        this.error = ''
        store.toast('两次输入的新密码不一致')
        return
      }
      const r = store.changePassword(this.oldPwd, this.newPwd)
      if (!r.ok) {
        this.error = r.message
        return
      }
      this.error = ''
      uni.showModal({
        title: '密码已修改',
        content: '下次登录请使用新密码',
        showCancel: false,
        success: () => uni.navigateBack()
      })
    }
  }
}
</script>

<style scoped>
.mt24 { margin-top: 24rpx; }
.mt20 { margin-top: 20rpx; }
</style>

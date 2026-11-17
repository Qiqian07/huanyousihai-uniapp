<template>
  <view class="page">
    <hys-navbar title="提现" back />

    <view class="wrap">
      <hys-field label="提现金额（元）" :help="'可提现余额 ¥' + (user.balance / 100).toFixed(2)" :error="error">
        <input
          class="field num"
          type="digit"
          v-model="amount"
          placeholder="请输入金额"
          placeholder-class="field-ph"
          :class="{ 'field-error': !!error }"
        />
        <view class="quick">
          <view v-for="q in quickList" :key="q" class="quick-c" @tap="amount = String(q)">
            <text class="quick-t num">¥{{ q.toFixed(2) }}</text>
          </view>
          <view class="quick-c" @tap="all">
            <text class="quick-t">全部</text>
          </view>
        </view>
      </hys-field>

      <view class="calc card">
        <text class="calc-t">提现计算式</text>
        <view class="kv">
          <text class="kv-k">提现金额</text>
          <hys-amount :value="cents" cents size="s" class="kv-v" />
        </view>
        <view class="kv">
          <text class="kv-k">手续费 6%</text>
          <hys-amount :value="fee" cents size="s" class="kv-v" />
        </view>
        <text class="calc-s">按分计算向下取整到分</text>
        <view class="kv total">
          <text class="kv-k strong">预计到账</text>
          <hys-amount :value="arrive" cents size="m" class="price-vip" />
        </view>
      </view>

      <hys-notice class="mt16" type="info" text="提现手续费 6%，按分计算向下取整到分" />
      <hys-notice class="mt12" type="info" text="建议用互转或升级 VIP 消费，避免手续费与个人所得税" />
      <hys-notice class="mt12" type="warning" text="提现将进入人工审核" />

      <view class="btn btn-primary mt24" :class="{ 'btn-disabled': !canSubmit }" @tap="submit">
        <text>{{ submitting ? '处理中…' : '提交提现申请' }}</text>
      </view>

      <!-- ST-S-05 已提交人工审核 -->
      <view v-if="result" class="result card">
        <view class="result-head">
          <hys-badge label="审核中" tone="warning" />
        </view>
        <text class="result-t">已提交人工审核</text>
        <view class="kv">
          <text class="kv-k">提现金额</text>
          <hys-amount :value="result.amount" cents size="s" class="kv-v" />
        </view>
        <view class="kv">
          <text class="kv-k">手续费</text>
          <hys-amount :value="result.fee" cents size="s" class="kv-v" />
        </view>
        <view class="kv">
          <text class="kv-k">预计到账</text>
          <hys-amount :value="result.arrive" cents size="s" class="kv-v" />
        </view>
        <view class="btn btn-secondary btn-sm mt16" @tap="go('/pages/wallet/income')">
          <text>查看流水</text>
        </view>
      </view>

      <view class="bottom-pad" />
    </view>
  </view>
</template>

<script>
import store from '@/store/index.js'
import { money, withdrawFee } from '@/utils/commission.js'

export default {
  data() {
    return {
      amount: '',
      error: '',
      submitting: false,
      result: null
    }
  },
  computed: {
    user() {
      return store.currentUser.value
    },
    quickList() {
      return [100, 500, 1000]
    },
    cents() {
      return money.toCents(Number(this.amount || 0))
    },
    fee() {
      return withdrawFee(this.cents)
    },
    arrive() {
      return Math.max(0, this.cents - this.fee)
    },
    canSubmit() {
      const n = Number(this.amount || 0)
      return n > 0 && !this.error
    }
  },
  watch: {
    amount(v) {
      const n = Number(v || 0)
      this.error = ''
      if (!v) return
      if (!(n > 0)) this.error = '请输入正确的提现金额'
      else if (money.toCents(n) > this.user.balance) this.error = '余额不足'
    }
  },
  methods: {
    all() {
      this.amount = (this.user.balance / 100).toFixed(2)
    },
    submit() {
      if (!this.canSubmit || this.submitting) return
      this.submitting = true
      setTimeout(async () => {
        const r = await store.withdraw(this.amount)
        this.submitting = false
        if (!r.ok) {
          this.error = r.message
          return
        }
        this.result = r.settle
        this.amount = ''
        store.toast('已提交人工审核')
      }, 420)
    },
    go(url) {
      uni.navigateTo({ url })
    }
  }
}
</script>

<style scoped>
.quick {
  display: flex;
  gap: 12rpx;
  margin-top: 14rpx;
  flex-wrap: wrap;
}
.quick-c {
  padding: 8rpx 20rpx;
  border: 1rpx solid var(--c-mist);
  border-radius: 999rpx;
}
.quick-t {
  font-size: 22rpx;
  color: var(--c-ink);
}
.calc {
  padding: 24rpx;
}
.calc-t {
  display: block;
  font-size: 26rpx;
  font-weight: 600;
  color: var(--c-ink);
  padding-bottom: 10rpx;
  border-bottom: 1rpx solid var(--c-mist);
}
.calc-s {
  display: block;
  font-size: 22rpx;
  color: var(--c-slate);
  padding: 4rpx 0 8rpx;
}
.total {
  border-top: 1rpx solid var(--c-mist);
  margin-top: 8rpx;
  padding-top: 18rpx;
}
.strong {
  font-weight: 600;
  color: var(--c-ink);
}
.mt16 { margin-top: 16rpx; }
.mt12 { margin-top: 12rpx; }
.mt24 { margin-top: 24rpx; }
.result {
  margin-top: 24rpx;
  padding: 24rpx;
}
.result-head { margin-bottom: 12rpx; }
.result-t {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: var(--c-ink);
  margin-bottom: 8rpx;
}
.bottom-pad { height: 60rpx; }
</style>

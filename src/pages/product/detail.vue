<template>
  <view class="page">
    <hys-navbar title="商品详情" back />

    <scroll-view class="scroll" scroll-y v-if="item">
      <view class="hero">
        <image class="hero-img" :src="item.cover" mode="aspectFill" />
      </view>

      <view class="wrap body">
        <text class="title">{{ item.title }}</text>
        <view class="meta">
          <text class="m">{{ item.origin }}</text>
          <text class="m">已售 {{ item.sales }}</text>
        </view>

        <view class="price card">
          <view class="p-l">
            <text class="p-k">当前适用价</text>
            <hys-amount :value="price" cents size="l" class="price-vip" />
          </view>
          <view class="p-r">
            <view class="p-line">
              <text class="p-k2">普通价</text>
              <hys-amount :value="item.normalPrice" cents size="s" class="price-normal" />
            </view>
            <view class="p-line">
              <text class="p-k2">VIP 价</text>
              <hys-amount :value="item.vipPrice" cents size="s" class="price-vip" />
            </view>
          </view>
        </view>

        <view class="spec card">
          <view class="kv">
            <text class="kv-k">规格</text>
            <text class="kv-v">{{ item.spec }}</text>
          </view>
          <view class="kv">
            <text class="kv-k">产地</text>
            <text class="kv-v">{{ item.origin }}</text>
          </view>
          <view class="kv">
            <text class="kv-k">会员性质</text>
            <text class="kv-v">{{ isVip ? 'VIP 会员（享 VIP 价）' : '普通会员（普通价）' }}</text>
          </view>
        </view>

        <text class="desc">{{ item.desc }}</text>

        <hys-notice class="mt16" type="info" text="价值 3980 元的 AI 智能相册网站，升级 VIP 会员仅需 398 元" />
        <view class="dock-pad" />
      </view>
    </scroll-view>

    <view class="dock">
      <view class="dock-l">
        <text class="dock-k">当前适用价</text>
        <hys-amount :value="price" cents size="m" class="price-vip" />
      </view>
      <view class="btn btn-primary dock-btn" @tap="buy">
        <text>立即购买</text>
      </view>
    </view>
  </view>
</template>

<script>
import store from '@/store/index.js'
import { guards } from '@/utils/commission.js'

export default {
  data() {
    return { item: null }
  },
  computed: {
    isVip() {
      return guards.isVipActive(store.currentUser.value)
    },
    price() {
      return this.item ? guards.priceFor(this.item, store.currentUser.value) : 0
    }
  },
  onLoad(q) {
    this.item = store.state.products.find((p) => p.id === q.id) || store.state.products[0]
  },
  methods: {
    async buy() {
      const it = this.item
      const order = await store.createOrder({
        kind: '商品',
        itemId: it.id,
        title: it.title,
        spec: it.spec + ' · 1 件',
        amount: this.price,
        thumb: it.cover
      })
      if (!order) return /* API 模式失败时内部已提示 */
      store.toast('下单成功，待付款')
      setTimeout(() => uni.switchTab({ url: '/pages/order/index' }), 500)
    }
  }
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}
.scroll { flex: 1; height: 0; }
.hero {
  position: relative;
  width: 100%;
  height: 0;
  padding-bottom: 100%;
  background: var(--c-mist);
}
.hero-img {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
}
.body { padding-top: 28rpx; }
.title {
  font-size: 44rpx;
  font-weight: 700;
  color: var(--c-ink);
}
.meta {
  display: flex;
  gap: 20rpx;
  margin-top: 10rpx;
}
.m {
  font-size: 24rpx;
  color: var(--c-slate);
}
.price {
  margin-top: 24rpx;
  padding: 24rpx;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.p-k { font-size: 22rpx; color: var(--c-slate); }
.p-l { display: flex; flex-direction: column; gap: 6rpx; }
.p-r { display: flex; flex-direction: column; gap: 8rpx; align-items: flex-end; }
.p-line { display: flex; align-items: baseline; gap: 8rpx; }
.p-k2 { font-size: 22rpx; color: var(--c-slate); }
.spec {
  margin-top: 24rpx;
  padding: 8rpx 24rpx;
}
.desc {
  display: block;
  margin-top: 24rpx;
  font-size: 28rpx;
  line-height: 1.8;
  color: var(--c-ink);
}
.mt16 { margin-top: 16rpx; }
.dock-l { display: flex; flex-direction: column; gap: 4rpx; }
.dock-k { font-size: 22rpx; color: var(--c-slate); }
.dock-btn { width: 320rpx; flex-shrink: 0; }
</style>

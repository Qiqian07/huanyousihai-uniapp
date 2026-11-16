<template>
  <view class="page">
    <hys-navbar title="环游四海 · 后台" back />

    <view class="wrap">
      <scroll-view scroll-x class="mods" :show-scrollbar="false">
        <view class="mods-row">
          <view
            v-for="m in modules"
            :key="m.key"
            class="mod"
            :class="{ on: tab === m.key }"
            @tap="tab = m.key"
          >
            <text class="mod-t">{{ m.name }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- 充值 -->
      <view v-if="tab === 'recharge'" class="pane">
        <text class="pane-t">充值</text>
        <text class="pane-s">单笔金额输入 + 5000 元上限校验，超额拒绝并提示拆分</text>
        <hys-field label="会员账号" required>
          <input class="field num" v-model="phone" placeholder="输入会员手机号" placeholder-class="field-ph" maxlength="11" />
        </hys-field>
        <hys-field label="充值金额（元）" required :error="rechargeError">
          <input class="field num" v-model="amount" type="digit" placeholder="单笔不超过 5000 元" placeholder-class="field-ph" />
        </hys-field>
        <view class="btn btn-primary" :class="{ 'btn-disabled': !rechargeOk }" @tap="doRecharge">
          <text>确认充值</text>
        </view>
      </view>

      <!-- 提现审核 -->
      <view v-if="tab === 'withdraw'" class="pane">
        <text class="pane-t">提现审核</text>
        <text class="pane-s">待审核列表 + 通过 / 拒绝，金额与手续费明细同屏</text>
        <view v-for="w in pendingWithdraw" :key="w.id" class="audit card">
          <view class="audit-head">
            <text class="audit-t">{{ userName(w.userId) }}</text>
            <hys-badge label="待审核" tone="warning" />
          </view>
          <view class="kv">
            <text class="kv-k">提现金额</text>
            <hys-amount :value="w.amount" cents size="s" class="kv-v" />
          </view>
          <view class="kv">
            <text class="kv-k">手续费 6%</text>
            <hys-amount :value="w.fee || 0" cents size="s" class="kv-v" />
          </view>
          <view class="kv">
            <text class="kv-k">预计到账</text>
            <hys-amount :value="w.amount - (w.fee || 0)" cents size="s" class="kv-v" />
          </view>
          <view class="audit-acts">
            <view class="btn btn-secondary btn-sm act" @tap="audit(w, '拒绝')">
              <text>拒绝</text>
            </view>
            <view class="btn btn-primary btn-sm act" @tap="audit(w, '通过')">
              <text>通过</text>
            </view>
          </view>
        </view>
        <hys-state v-if="!pendingWithdraw.length" type="empty" title="暂无待审核提现" desc="资金类结果返回前不提前更新数字" />
      </view>

      <!-- 上架 -->
      <view v-if="tab === 'publish'" class="pane">
        <text class="pane-t">上架</text>
        <text class="pane-s">商品 / 线路素材：图、视频、按天行程录入</text>
        <view class="mlist card">
          <view class="mrow">
            <text class="mk">素材</text>
            <text class="mv">图片（16:10 封面）+ 视频 + 酒店 / 餐饮 / 景区分组图集</text>
          </view>
          <view class="mrow">
            <text class="mk">行程</text>
            <text class="mv">按天录入：日戳 + 集合时间 / 地点 + 文案 + 图片</text>
          </view>
          <view class="mrow">
            <text class="mk">双价</text>
            <text class="mv">普通会员价 / VIP 会员价，VIP 价仅在有效期内生效</text>
          </view>
          <view class="mrow">
            <text class="mk">合规</text>
            <text class="mv">398 口径固定；分销只允许两级，页面不得出现第三级图形</text>
          </view>
        </view>
        <view class="btn btn-secondary mt16" @tap="todo('上架')">
          <text>上传素材并上架</text>
        </view>
      </view>

      <!-- 相册上传 -->
      <view v-if="tab === 'album'" class="pane">
        <text class="pane-t">相册上传</text>
        <text class="pane-s">按参团批次代传照片 / 视频</text>
        <view class="mlist card">
          <view v-for="a in albums" :key="a.id" class="mrow">
            <text class="mk">{{ a.season }}</text>
            <text class="mv">{{ a.route }} · 照片 {{ a.photos }} / 视频 {{ a.videos }}</text>
          </view>
        </view>
        <view class="btn btn-primary mt16" @tap="todo('相册上传')">
          <text>上传本批次素材</text>
        </view>
      </view>

      <!-- 异常对账 -->
      <view v-if="tab === 'gap'" class="pane">
        <text class="pane-t">异常对账 · referrer_missing</text>
        <text class="pane-s">账本精确视图：金额右对齐等宽，无插画</text>

        <view class="alert">
          <hys-icon name="error" :size="32" tone="danger" />
          <view class="alert-main">
            <text class="alert-t">计佣已中断：推荐关系缺失（referrer_missing），请补绑后重算</text>
            <text class="alert-s">不提供顺延 / 改派 / 少发出账操作</text>
          </view>
        </view>

        <view class="tbl card">
          <view class="tbl-head">
            <text class="c1">订单号</text>
            <text class="c2">类型</text>
            <text class="c3">金额</text>
            <text class="c4">异常码</text>
          </view>
          <view v-for="(g, i) in gapRows" :key="i" class="tbl-row">
            <text class="c1 num">{{ g.no }}</text>
            <text class="c2">{{ g.type }}</text>
            <text class="c3 num">{{ g.amount }}</text>
            <text class="c4 danger">{{ g.code }}</text>
          </view>
        </view>
        <text class="tbl-note">payout_gap 正常为空，仅异常数据才出现记录</text>

        <view class="btn btn-secondary mt16" @tap="todo('补绑后重算')">
          <text>补绑推荐人后重算</text>
        </view>
        <hys-notice class="mt16" type="plain" text="破坏性操作（拒绝 / 下架 / 重算）必须二次确认；资金类结果返回前不得提前更新数字" />
      </view>

      <view class="bottom-pad" />
    </view>
  </view>
</template>

<script>
import store from '@/store/index.js'
import { money, guards } from '@/utils/commission.js'

export default {
  data() {
    return {
      tab: 'gap',
      phone: '',
      amount: '',
      rechargeError: '',
      modules: [
        { key: 'recharge', name: '充值' },
        { key: 'withdraw', name: '提现审核' },
        { key: 'publish', name: '上架' },
        { key: 'album', name: '相册上传' },
        { key: 'gap', name: '异常对账' }
      ],
      gapRows: [
        { no: '—', type: '线路订单', amount: '¥0.00', code: 'referrer_missing' },
        { no: '—', type: '商品订单', amount: '¥0.00', code: 'payout_gap' },
        { no: '—', type: '提现', amount: '¥0.00', code: '—' },
        { no: '—', type: '充值', amount: '¥0.00', code: '—' }
      ]
    }
  },
  computed: {
    albums() {
      return store.state.albums
    },
    pendingWithdraw() {
      return store.state.wallet.filter((w) => w.type === '提现' && w.status === '审核中')
    },
    rechargeOk() {
      const n = Number(this.amount || 0)
      return this.phone && n > 0 && !this.rechargeError
    }
  },
  watch: {
    amount(v) {
      const n = Number(v || 0)
      this.rechargeError = ''
      if (!v) return
      if (n > 5000) this.rechargeError = '单笔充值不超过 5000 元，请拆分为多次充值'
    }
  },
  methods: {
    userName(id) {
      const u = store.userById(id)
      return u ? u.name : '—'
    },
    doRecharge() {
      if (!this.rechargeOk) return
      const target = store.userByPhone(this.phone)
      if (!target) {
        this.rechargeError = '未找到该会员账号'
        return
      }
      const check = guards.checkRecharge(money.toCents(this.amount))
      if (!check.ok) {
        this.rechargeError = check.message
        return
      }
      target.balance += money.toCents(this.amount)
      store.state.wallet.unshift({
        id: 'W' + (store.state.wallet.length + 1),
        userId: target.id,
        type: '充值',
        amount: money.toCents(this.amount),
        counterparty: '分公司代充',
        status: '已入账',
        createdAt: '刚刚'
      })
      this.amount = ''
      store.toast('充值成功')
    },
    audit(w, action) {
      uni.showModal({
        title: '二次确认',
        content: `确认${action} ${this.userName(w.userId)} 的提现 ¥${(w.amount / 100).toFixed(2)}？`,
        success: (res) => {
          if (!res.confirm) return
          w.status = action === '通过' ? '已到账' : '已拒绝'
          store.toast('已' + action)
        }
      })
    },
    todo(name) {
      store.toast(name + '：演示版本仅提供入口')
    }
  }
}
</script>

<style scoped>
.mods { width: 100%; white-space: nowrap; margin-top: 10rpx; }
.mods-row { display: inline-flex; gap: 12rpx; }
.mod {
  flex-shrink: 0;
  padding: 12rpx 24rpx;
  border: 1rpx solid var(--c-mist);
  border-radius: 999rpx;
  background: var(--c-paper);
}
.mod.on {
  background: var(--c-pine);
  border-color: var(--c-pine);
}
.mod-t {
  white-space: nowrap;
  font-size: 24rpx;
  color: var(--c-slate);
}
.mod.on .mod-t {
  color: var(--c-on-pine);
  font-weight: 600;
}
.pane { padding-top: 28rpx; }
.pane-t {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: var(--c-ink);
}
.pane-s {
  display: block;
  margin: 8rpx 0 24rpx;
  font-size: 22rpx;
  color: var(--c-slate);
}
.audit {
  padding: 20rpx 24rpx;
  margin-bottom: 16rpx;
}
.audit-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12rpx;
  border-bottom: 1rpx solid var(--c-mist);
}
.audit-t {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--c-ink);
}
.audit-acts {
  display: flex;
  gap: 16rpx;
  margin-top: 16rpx;
}
.act { flex: 1; }
.mlist { padding: 8rpx 24rpx; }
.mrow {
  display: flex;
  gap: 20rpx;
  padding: 18rpx 0;
  border-bottom: 1rpx solid var(--c-mist);
}
.mrow:last-child { border-bottom: none; }
.mk {
  width: 90rpx;
  font-size: 24rpx;
  color: var(--c-slate);
  flex-shrink: 0;
}
.mv {
  flex: 1;
  font-size: 24rpx;
  color: var(--c-ink);
  line-height: 1.6;
}
.alert {
  display: flex;
  gap: 12rpx;
  padding: 20rpx;
  border: 1rpx solid rgba(178, 59, 46, 0.4);
  border-radius: 16rpx;
  background: var(--c-paper);
  margin-bottom: 20rpx;
}
.alert-t {
  display: block;
  font-size: 24rpx;
  font-weight: 600;
  color: var(--c-danger);
  line-height: 1.5;
}
.alert-s {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: var(--c-slate);
}
.tbl { padding: 0 20rpx; }
.tbl-head,
.tbl-row {
  display: flex;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid var(--c-mist);
}
.tbl-head .c1,
.tbl-head .c2,
.tbl-head .c3,
.tbl-head .c4 {
  font-size: 20rpx;
  color: var(--c-slate);
}
.c1 { flex: 1.1; font-size: 22rpx; color: var(--c-ink); }
.c2 { flex: 1; font-size: 22rpx; color: var(--c-ink); }
.c3 { flex: 0.9; font-size: 22rpx; color: var(--c-ink); text-align: right; }
.c4 { flex: 1.3; font-size: 20rpx; color: var(--c-slate); text-align: right; }
.danger { color: var(--c-danger); }
.tbl-note {
  display: block;
  margin-top: 12rpx;
  font-size: 20rpx;
  color: var(--c-slate);
}
.mt16 { margin-top: 16rpx; }
.bottom-pad { height: 60rpx; }
</style>

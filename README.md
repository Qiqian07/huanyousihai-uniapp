# 环游四海 V1.0 · uni-app 实现

按 `环游四海(2).pdf`（设计系统 + P-01~P-16 页面稿 + 浅色/夜间双主题）、`小程序1.0开发方案(1).doc`、`AGENT.md`（业务规则 / 佣金算法 / 合规红线）三份材料，用 **uni-app（Vue3 + Vite）** 复刻的完整可运行工程。

- 22 个页面 / 23 个业务组件，覆盖 PDF 全部手机端界面（含夜间稿）与后台「异常对账」页
- H5 与微信小程序双端可构建
- 佣金引擎附带 24 条单元测试，全部通过
- 金额全链路以「分」存储，展示两位小数

---

## 一、快速开始

```bash
cd hys-uniapp
npm install

npm run dev:h5          # H5 开发预览 → http://localhost:5180（带热更新，日常开发用这个）
npm run build:h5        # H5 生产构建 → dist/build/h5
npm run preview         # 起本地 HTTP 服务预览构建产物 → http://127.0.0.1:5181
npm run dev:mp-weixin   # 微信小程序开发构建 → dist/dev/mp-weixin
npm run build:mp-weixin # 微信小程序生产构建 → dist/build/mp-weixin
npm test                # 佣金引擎单元测试（24 条）
```

### ⚠️ 不要双击 `dist/build/h5/index.html`

uni-app 的 H5 产物是 ES module，**用 `file://` 打开会被浏览器 CORS 拦掉脚本和样式，页面白屏**（控制台报
`Access to script at 'file:///...' from origin 'null' has been blocked by CORS`）。必须走 `http://` 访问。

三种正确打开方式，任选其一：

| 方式 | 操作 | 地址 |
| --- | --- | --- |
| 开发模式（推荐） | `npm run dev:h5` | http://localhost:5180 |
| 预览构建产物 | `npm run build:h5` 后 `npm run preview` | http://127.0.0.1:5181 |
| Windows 双击 | 双击工程根目录的 `start.bat` | 自动开浏览器 |

`start.bat` / `serve.mjs` 是零依赖静态服务器：端口被占用会自动顺延，找不到构建产物会直接提示先 `npm run build:h5`。

微信小程序：用微信开发者工具「导入项目」，目录选 `dist/build/mp-weixin`（`manifest.json` 里的 `mp-weixin.appid` 需填自己的 AppID）。

### 夜间稿怎么看

| 端 | 方式 |
| --- | --- |
| H5 | URL 追加 `?theme=dark`，或在浏览器/系统里切到深色外观（`prefers-color-scheme`） |
| 微信小程序 | 跟随系统深色模式（`manifest.json` 已开 `darkmode: true`） |

---

## 二、PDF 页面 ↔ 代码对照

手机稿共 16 屏（P-01~P-16），每屏都有浅色 + 夜间两套；全部落到 `src/pages/`。

| PDF | 页面 | 代码路径 | 关键内容 |
| --- | --- | --- | --- |
| P-01 | 首页 | `pages/index/index` | 品牌头 + 全部/组团游/自驾游分段筛选 + 精选线路卡（普通价 / VIP 价双价） |
| P-02 | 线路详情 | `pages/route/detail` | 16:10 封面 + 视频入口 + 当前适用价（VIP 价已生效标签）+ 集合时间/地点 + 行程票根（DAY 竖条 + 缺口）+ 底部报名栏 |
| P-03 | 商城 | `pages/mall/index` | VIP 权益卡（398 统一口径）+ 特产双列瀑布 + 余额提示 + 订单归口说明 |
| P-04 | 订单中心 | `pages/order/index` | 待付款 / 待收货 / 售后单 / 全部订单 + 订单卡 |
| P-05 | 个人中心 | `pages/mine/index` | 会员卡（头像 + VIP 徽标 + 区域）+ 余额卡（充值/互转/提现）+ 资金与收益 / 分享 / 账号 / 更多服务 |
| P-06 | 我的分享 | `pages/share/index` | 推荐人 / 团队人数 / 个人人数 三项统计 + 直推名单 + 生成海报入口 |
| P-07 | 收入明细 | `pages/wallet/income` | 余额流水：充值 / 互转 / 提现 / 佣金 |
| P-08 | 充值 / 互转 | `pages/wallet/recharge` | 双 tab；充值单笔 ≤ 5000、互转对手方手机号 + 备注 |
| P-09 | 提现 | `pages/wallet/withdraw` | 金额快捷档 + 提现计算式（手续费 6% 按分向下取整）+ 三条提示 + 提交审核 |
| P-10 | 佣金明细 | `pages/commission/index` | 本期佣金合计 + 直推/间推明细 + 状态（已发放 / 待发放 / 冻结） |
| P-11 | 注册 | `pages/auth/register` | 推荐人码（必填）+ 名称 / 账号 / 手机号 / 省市区 / 密码 + 字段级校验 |
| P-12 | 生成二维码海报 | `pages/poster/index` | 海报画布（风景 + 小程序码安全区 + 生成中蒙层）+ 4 套模板 + 保存/分享 |
| P-13 | VIP 权益 | `pages/vip/index` | 398 开通 + 权益说明 + 续费 99 元/年 |
| P-14 | 占位页 | `pages/placeholder/index?name=绿色积分` | 「即将上线」通用页（绿色积分 / 优惠券 / 积分兑换 / APP） |
| P-15 | 相册站首页 | `pages/album/index` | 按参团批次的时间线 + 批次卡 |
| P-16 | 相册站详情 | `pages/album/detail` | 酒店 / 餐饮 / 景区 tab + 横滑图组 + 视频封面 |

补充页（PDF 未单列，但链路必需）：

| 页面 | 代码路径 | 说明 |
| --- | --- | --- |
| 商品详情 | `pages/product/detail` | 特产详情 + 立即购买 → 进入订单中心 |
| 订单详情 | `pages/order/detail` | 订单号 / 下单时间 / 出行日期 / 实付 / 支付 |
| 登录 | `pages/auth/login` | 手机号 + 密码；可选体验账号一键切换 |
| 修改密码 / 忘记密码 | `pages/auth/password` · `pages/auth/forgot` | 入口在个人中心 |
| 后台异常对账 | `pages/admin/index` | 充值 / 提现审核 / 上架 / 相册上传 / 异常对账 五模块，对账表 + 补绑重算 + 二次确认提示 |

---

## 三、设计系统落地

| 事项 | 做法 |
| --- | --- |
| 令牌 | `src/uni.scss` 为 SCSS 变量（编译期），`src/styles/common.scss` 为语义 CSS 变量（运行期）：`--c-ink / --c-pine / --c-terracotta / --c-sand / --c-paper / --c-mist / --c-slate / --c-vip-surface …` |
| 双主题 | 浅 / 夜两套变量：`@media (prefers-color-scheme: dark)` + H5 用 `html[data-theme]` 覆盖 |
| tabBar | 原生组件不吃 CSS 变量 → `src/utils/theme.js` 的 `applyTabBarTheme()` 运行时调 `setTabBarStyle` / `setTabBarItem`，并另备一套浅色图标（`static/tabbar/*-d.png`、`*-don.png`） |
| 金额 | 全部以「分」整数存储，展示用 `hys-amount`（`group` 默认关闭，即不加千分位） |
| 图标 | Lucide 24 网格 / 1.5 stroke，`src/utils/icons.js` 转 base64 SVG，供 `<image>` 使用（小程序不支持内联 SVG 标签） |
| 字体字阶 | 28 / 26 / 24 / 22 / 20 rpx 五档 + 数字用 `font-variant-numeric: tabular-nums` 对齐 |
| 骨架屏 | `hys-state` 的 `loading` 与真实布局同构，不用转圈占位 |

---

## 四、业务规则落地（AGENT.md）

佣金引擎：`src/utils/commission.js`（纯函数，无副作用，可直接单测）

| 红线 | 实现 |
| --- | --- |
| 分销只有两级 | `MAX_DEPTH = 2`，`settleUpgrade` 最多产出 DIRECT + INDIRECT 两条；`assertRecord` 遇第 3 级直接抛错 |
| 每单拨比恒 200 元 | `PAYOUT_TOTAL = 20000`，`assertRecord` 校验合计，不等即抛 `[COMPLIANCE]` |
| 直推序号 ≤2 → 100+100 | `payoutFor(1|2) → { direct: 10000, indirect: 10000 }` |
| 直推序号 ≥3 → 200+0 | `payoutFor(n≥3) → { direct: 20000, indirect: 0 }` |
| 推荐关系缺失 | 中断计佣 `aborted: true`、`gap.reason = 'referrer_missing'`、写 `payout_gap`，**禁止顺延 / 改派 / 少发** |
| 提现手续费 6% | `withdrawFee` = `Math.floor(cents * 6 / 100)`，整数运算按分向下取整（3333 分 → 199 分） |
| 单笔充值 ≤ 5000 | `guards.checkRecharge` 超限返回 `RECHARGE_LIMIT` + 建议拆分次数 |
| VIP 398 统一口径 | `VIP_PRICE = 39800`；商城、VIP 页、海报、后台文案一致 |
| 注册必填推荐人 | `register()` 校验推荐人码，缺失即拒绝注册（关系链不能断） |
| VIP 过期 | `guards.isVipActive` / `priceFor` / `vipState`（有效 / 即将到期 ≤30 天 / 已过期） |

`npm test` 覆盖 AGENT.md §8.2 合规用例与 §8.3 佣金计算用例，共 24 条，含：

- 我推荐第 1/2/3/9 人升级的拨比拆分
- 我第 1/3 个直推的下级再推荐一人（间推 100）
- 脏数据 `referrer_missing` 中断计佣
- A→B→C→D 四层链路：D 升级只惠及 C 与 B，A 不参与
- 1~30 号直推订单合计恒 200 且层级 ≤ 2
- 充值 5001 拒绝 / 5000 通过 / 1 万拆两次
- 提现 1000 → 手续费 60、到账 940；提现 33.33 → 1.99 / 31.34
- 篡改合计、伪造第三级受益人时断言拦截
- Q-03 续费不计佣、Q-04 VIP 过期回落普通价

---

## 五、数据与状态

- `src/store/data.js`：脱敏种子数据（无真实手机号/密码），推荐树 `平台运营 → 苏芮 → 林岚 → 周野（当前登录）→ 陈一 / 何雨 / 郑途 → 白露`
- `src/store/index.js`：`reactive` 状态 + 动作 `register / recharge / transfer / withdraw / createOrder / payOrder / upgradeVip / renewVip / changePassword / resetPassword`
- 升级 VIP 会**真实写两条佣金记录**并加到受益人余额，可在「佣金明细」里看到
- 切换当前登录人：个人中心 → 退出登录 → 登录页「体验账号」

---

## 六、自检记录

| 项 | 结果 |
| --- | --- |
| `npm run build:h5` | 通过（仅 Sass `@import` 与 legacy JS API 弃用告警，不影响产物） |
| `npm run build:mp-weixin` | 通过，22 页 + tabBar 图标全部产出 |
| `npm test` | 24 / 24 通过 |
| 界面截图 | `shots/` 下 21 页 × 浅色/夜间 = 42 张，另有 4 张交互流程截图 |
| 交互流程 | 提现 1000 → 手续费 60.00 / 到账 940.00；充值 5001 → 提示「单笔充值不超过 5000 元，请拆分为多次充值」；注册空提交 → 6 条字段级报错；商品立即购买 → 订单中心出现「云南野生菌礼盒 / 商品 / 待付款 / ¥168.00」 |

重新生成截图（需本机安装 Edge）：

```bash
npm run build:h5          # 先构建
npm run preview           # 另开一个终端，保持运行（http://127.0.0.1:5181）
npm run shot              # 浅色
SHOT_THEME=dark npm run shot   # 夜间
```

---

## 七、待确认项

| 编号 | 事项 | 当前处理 |
| --- | --- | --- |
| Q-03 | 99 元续费是否计佣、比例多少 | 默认 `renew_commission_rate = 0`（不计佣），`settleRenew` 返回 `pending: 'Q-03'` 并给出提示，不静默发钱 |
| Q-04 | VIP 过期后是否还享 VIP 价 | 已按「过期回落普通价」实现（`guards.priceFor`） |
| Q-04b | VIP 过期是否影响佣金发放 | 默认 `false`（不影响），集中在 `CONFIG` 一处 |

其余 Q-xx 项（区域分级、运费、发票等）在代码里以 `// Q-xx` 注释标出，改一处即可。

## 八、已知限制

- 全为前端实现，支付、短信、实名、提现打款均为**模拟**，无后端接口；`src/store` 可原样替换为真实 API。
- 相册站的视频为封面图 + 播放按钮占位，未接视频源。
- 商品图 / 线路图为演示素材，正式上线需替换为真实版权图片。
- `mp-weixin.appid` 为空，导入微信开发者工具前需自行填写。

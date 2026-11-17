# 环游四海 · RuoYi 全栈版（feat/ruoyi-fullstack 分支）

在原 uni-app 纯前端实现基础上，接入 **RuoYi SpringBoot 3.9.2 后端** 与 **RuoYi Vue3 管理后台**，形成完整全栈工程。前端支持双模式：**演示模式**（纯本地 mock，无需后端）与 **API 模式**（对接本仓库后端）。

```
huanyousihai-uniapp/
├── src/                  # uni-app 小程序端（22 页，双模式 store）
│   ├── api/              # [新增] 请求封装 + 业务 API
│   └── store/index.js    # [改造] API 模式 / 演示模式自动切换
├── ruoyi-springboot/     # RuoYi SpringBoot 后端
│   ├── ruoyi-hys/        # [新增] 环游四海业务模块
│   │   ├── engine/       # 佣金引擎（纯函数，与前端 JS 版语义对齐）
│   │   ├── domain/       # 会员/线路/商品/订单/钱包流水/充值/提现/佣金/相册/对账缺口
│   │   ├── mapper/       # MyBatis 接口 + XML（显式 resultMap）
│   │   ├── service/      # 业务规则落地（事务、原子余额更新）
│   │   ├── security/     # 小程序会员 JWT 鉴权（独立于后台管理员体系）
│   │   └── controller/
│   │       ├── api/      # /api/hys/** 小程序端接口（6 个）
│   │       └── admin/    # /hys/** 管理端接口（7 个，PreAuthorize 权限）
│   └── ...               # RuoYi 原模块（仅 SecurityConfig 放行 /api/hys/**）
├── ruoyi-vue3/           # RuoYi Vue3 管理后台
│   ├── src/api/hys/      # [新增] 业务 API
│   └── src/views/hys/    # [新增] 会员/充值审核/提现审核/线路/商品/相册/异常对账 7 页
└── sql/hys.sql           # [新增] 建表 + 后台菜单权限 + 种子数据
```

## 一、快速启动（全栈）

### 1. 数据库
```bash
# 先导入 RuoYi 自带脚本（ry_20xx.sql / quartz.sql），再导入：
mysql -uroot -p < sql/hys.sql
```

### 2. 后端（Java 17+）
```bash
cd ruoyi-springboot
# 修改 ruoyi-admin/src/main/resources/application-druid.properties 数据库连接
mvn package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar   # 默认 8080
```

### 3. 管理后台
```bash
cd ruoyi-vue3
npm install
npm run dev    # 默认代理到 http://localhost:8080
```
登录后左侧菜单出现「环游四海管理」目录：会员管理 / 充值审核 / 提现审核 / 线路上架 / 商品上架 / 相册管理 / 异常对账。

### 4. 小程序端（API 模式）
`src/api/request.js` 中 `API_BASE` 指向后端地址（默认 `http://localhost:8080/api/hys`）。小程序端默认启用 API 模式；把本地存储 `hys_api_mode` 设为 `'0'` 即切换到演示模式（纯本地 mock，无需后端）。

## 二、佣金引擎（合规核心）

后端 `ruoyi-hys/engine/HysCommissionEngine.java` 与前端 `src/utils/commission.js` **语义逐条对齐**：

| 规则 | 实现 |
|------|------|
| 两级分销 | 仅直推（depth=1）与间推（depth=2）两层受益 |
| 每单恒 200 分 | 升级订单佣金总额恒定 200 分（¥2），超出部分为平台收入 |
| 直推/间推分成 | 按 70% / 30% 拆分 |
| 手续费 6% | 按分向下取整（整数运算，双端一致：3333 分 → 199 分） |
| 金额单位 | 全链路以「分」为单位的 long 存储，禁止浮点 |
| IEEE754 对齐 | `1.005*100` 双端同样得到 `100` 分（有对齐测试用例） |
| 计佣中断 | 推荐链缺失时**不落任何佣金账**，写入 `hys_recon_gap` 待人工补绑重算 |
| 幂等 | 佣金按 `order_id + beneficiary_id` 唯一约束，重复结算被拒绝 |

单元测试：`ruoyi-hys/src/test/.../HysCommissionEngineTest.java`（25 条，含 §8 合规用例）+ `HysVipStateTest.java`（4 条），`mvn -pl ruoyi-hys test` 全部通过。

## 三、钱包与资金安全

- 充值：后台审核通过才入账；拒绝即作废。
- 提现：申请时冻结余额（原子扣减），审核驳回自动退回；手续费按 6% 按分取整。
- 互转：余额原子扣减 + 不足回滚，双方各落一条流水。
- 所有资金变动均写 `hys_wallet_txn` 流水，余额可随时与流水对账。

## 四、鉴权体系（两套互不干扰）

- 后台管理员：RuoYi 自带 Spring Security + `@PreAuthorize('hys:xxx:xxx')`。
- 小程序会员：`ruoyi-hys/security` 自有 JWT（`Authorization: Bearer`），`/api/hys/auth/**` 免登，其余小程序接口经 `HysAuthInterceptor` 校验；`SecurityConfig` 中 `/api/hys/**` 已放行给自有拦截器处理。

## 五、测试与自检

| 项 | 命令 | 结果 |
|----|------|------|
| 前端佣金引擎测试 | `npm test` | 24/24 通过 |
| 后端佣金引擎测试 | `mvn -pl ruoyi-hys test` | 29/29 通过 |
| 后端全模块编译 | `mvn compile` | 通过 |
| H5 构建 | `npm run build:h5` | 通过 |

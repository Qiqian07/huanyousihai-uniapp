# 协作指南

面向加入本仓库的同学。第一次参与请按顺序做完 **1 → 3**，大约 5 分钟。

---

## 1. 把项目跑起来

需要 Node.js 18 或以上。先确认：

```bash
node -v     # 要能打印出 v18 以上，报「不是内部或外部命令」就是还没装
```

没装就去 https://nodejs.org/zh-cn/download 下 LTS 版，装完**关掉所有终端窗口**再重开（PATH 才会生效）。

```bash
git clone <仓库地址>
cd huanyousihai-uniapp
npm install
npm run dev:h5
```

浏览器打开 **http://localhost:5180** 就能看到效果，改代码会自动热更新。

不想敲命令的话，Windows 直接双击 `dev.bat`（开发模式）或 `start.bat`（预览构建产物）。这两个脚本会自己找 Node，不依赖系统 PATH。

> ⚠️ **不要双击 `dist/build/h5/index.html`**。uni-app 的 H5 产物是 ES module，`file://` 协议下会被浏览器 CORS 拦掉，页面白屏。必须走 `http://`。

想在小程序里看：`npm run build:mp-weixin`，用微信开发者工具导入 `dist/build/mp-weixin`（`src/manifest.json` 的 `mp-weixin.appid` 填自己的测试号）。

---

## 2. 代码在哪

```
src/
├── pages/          # 22 个页面，路径与 pages.json 一一对应
├── components/     # 23 个业务组件，hys- 前缀，easycom 自动引入
├── store/
│   ├── data.js     # 种子数据（脱敏：无真实手机号/密码）+ 固定合规文案
│   └── index.js    # 全局状态与动作（注册/充值/互转/提现/下单/升级 VIP）
├── utils/
│   ├── commission.js  # 佣金引擎（纯函数，改动前先跑 npm test）
│   ├── icons.js       # Lucide 图标 → base64 SVG
│   └── theme.js       # 浅色/夜间主题取值 + tabBar 同步
├── styles/common.scss # 全局语义 CSS 变量、卡片、按钮、输入框等基础类
└── static/            # 图片、tabBar 图标、favicon

tests/commission.test.mjs  # 佣金引擎 24 条用例
scripts/find-node.cmd      # 双击脚本用的 Node 探测逻辑
serve.mjs                  # 零依赖静态服务器
start.bat / dev.bat / push.bat
```

设计稿对照关系、规则落地方式见根目录 `README.md`（含 PDF P-01~P-16 ↔ 页面路径的完整表格）。

---

## 3. 改之前先知道的几条硬约束

这些来自 `AGENT.md`，是业务红线，**改代码时不能破坏**：

- 分销只有两级（直推 + 间推），没有第三级，不顺延、不改派
- 每笔 398 升级订单，佣金合计恒为 200.00 元
- 提现手续费 6%，按「分」向下取整
- 单笔充值不超过 5000 元，超过要提示拆分
- 注册必须填推荐人码（关系链不能断）
- 金额一律用「分」的整数存储，不要用浮点数

`src/utils/commission.js` 里有 `// COMPLIANCE` 注释标出这些位置。

---

## 4. 提交前自检

```bash
npm test          # 佣金引擎 24 条用例，必须全绿
npm run build:h5  # 确认能构建通过
```

两项都过再提交。`npm test` 有失败就别 merge，说明拨比或手续费算错了。

---

## 5. 分支与提交

```bash
git checkout -b feat/你的功能名
# 改完
git add -A
git commit -m "feat: 做了什么"
git push origin feat/你的功能名
```

然后在 GitHub 上发起 Pull Request，等 review 合并，不要直接往 `main` 推。

提交信息前缀：

| 前缀 | 用于 |
| --- | --- |
| `feat:` | 新页面 / 新功能 |
| `fix:` | 修 bug |
| `style:` | 只调样式、不改逻辑 |
| `docs:` | 文档 |
| `refactor:` | 重构，行为不变 |

---

## 6. 不要提交的东西

`.gitignore` 已经挡住了，别手动 `git add -f` 绕过去：

- `node_modules/`（231 MB）
- `dist/`（构建产物，`npm run build:h5` 随时能再生成）
- 编辑器配置（`.vscode/`、`.idea/`）

---

## 7. 常见问题

**页面白屏 / 打不开**
先看浏览器控制台。如果是 `blocked by CORS` + `file://`，就是用了双击打开，改用 `npm run dev:h5`。

**`npm install` 卡住或报错**
换淘宝镜像：`npm config set registry https://registry.npmmirror.com`，再重新 `npm install`。

**夜间稿怎么预览**
`http://localhost:5180/?theme=dark`，或把系统外观切成深色。

**想换登录身份看不同数据**
个人中心 → 退出登录 → 登录页选「体验账号」。种子推荐树：
平台运营 → 苏芮 → 林岚 → 周野（默认登录）→ 陈一 / 何雨 / 郑途 → 白露

**改了 `commission.js` 测试挂了**
看输出里哪条用例失败，`实际` 和 `期望` 两行会直接告出差异。别改测试去迁就代码 —— 断言是从 `AGENT.md` 抄下来的。

/* ============================================================================
   环游四海 V1.0 · 种子数据（脱敏样例，不含任何真实手机号与密码）
   金额统一以「分」存储，展示层格式化为元。
   推荐树：平台运营种子 → 苏芮 → 林岚 → 周野（当前登录）→ 陈一 / 何雨 / 郑途 → 白露
   ========================================================================== */

const IMG = {
  dali: '/static/img/route-dali.jpg',
  daocheng: '/static/img/route-daocheng.jpg',
  erhai: '/static/img/route-erhai.jpg',
  miao: '/static/img/route-miao.jpg',
  mushroom: '/static/img/product-mushroom.jpg',
  tiedye: '/static/img/product-tiedye.jpg',
  tea: '/static/img/product-tea.jpg',
  flower: '/static/img/product-flower.jpg',
  poster: '/static/img/poster-landscape.jpg'
}

export const IMAGES = IMG

export const users = [
  {
    id: 'u-seed', name: '平台运营中心', phone: '13800000000',
    region: ['云南省', '昆明市', '五华区'], referrerId: null,
    level: 'VIP', vipExpireAt: '2029-01-01', balance: 0, greenPoints: 0,
    createdAt: '2025-10-01', upgradedAt: '2025-10-01', hidden: true
  },
  {
    id: 'u-s', name: '苏芮', phone: '13900000001',
    region: ['云南省', '昆明市', '盘龙区'], referrerId: 'u-seed',
    level: 'VIP', vipExpireAt: '2027-09-12', balance: 86000, greenPoints: 43,
    createdAt: '2025-11-02', upgradedAt: '2025-11-02'
  },
  {
    id: 'u-a', name: '林岚', phone: '13800000102',
    region: ['云南省', '大理白族自治州', '大理市'], referrerId: 'u-s',
    level: 'VIP', vipExpireAt: '2027-06-30', balance: 152000, greenPoints: 76,
    createdAt: '2026-01-15', upgradedAt: '2026-01-15'
  },
  {
    id: 'u-b', name: '周野', phone: '13800000203',
    region: ['四川省', '成都市', '武侯区'], referrerId: 'u-a',
    level: 'VIP', vipExpireAt: '2027-03-08', balance: 128000, greenPoints: 64,
    createdAt: '2026-03-08', upgradedAt: '2026-03-08'
  },
  {
    id: 'u-c1', name: '陈一', phone: '13800000304',
    region: ['广东省', '广州市', '天河区'], referrerId: 'u-b',
    level: 'VIP', vipExpireAt: '2027-05-22', balance: 10000, greenPoints: 5,
    createdAt: '2026-05-20', upgradedAt: '2026-05-22'
  },
  {
    id: 'u-c2', name: '何雨', phone: '13800000405',
    region: ['浙江省', '杭州市', '西湖区'], referrerId: 'u-b',
    level: 'VIP', vipExpireAt: '2027-06-13', balance: 0, greenPoints: 0,
    createdAt: '2026-06-11', upgradedAt: '2026-06-13'
  },
  {
    id: 'u-c3', name: '郑途', phone: '13800000506',
    region: ['贵州省', '黔东南苗族侗族自治州', '凯里市'], referrerId: 'u-b',
    level: 'VIP', vipExpireAt: '2027-07-05', balance: 0, greenPoints: 0,
    createdAt: '2026-07-02', upgradedAt: '2026-07-05'
  },
  {
    id: 'u-d1', name: '白露', phone: '13800000607',
    region: ['北京市', '北京市', '海淀区'], referrerId: 'u-c1',
    level: 'VIP', vipExpireAt: '2027-08-21', balance: 0, greenPoints: 5,
    createdAt: '2026-08-19', upgradedAt: '2026-08-21'
  }
]

export const routes = [
  {
    id: 'r-dali', type: '组团游', title: '滇西秘境 · 大理丽江',
    subtitle: '6 天 5 晚｜昆明集合接站｜苍山洱海 · 玉龙雪山 · 束河古镇',
    days: 6, nights: 5, cover: IMG.dali,
    normalPrice: 298000, vipPrice: 268000,
    departCity: '昆明', capacity: 24, sold: 18, rating: '4.9',
    gather: { time: '参团首日 09:00', place: '大理站 · 出站口 A' },
    tags: ['含全程酒店', '含 5 早 8 正', '零自费'],
    selfDrive: false,
    video: true,
    itinerary: [
      { day: 'DAY 01', title: '大理古城 · 苍山索道', meet: '参团首日 09:00 · 大理站', copy: '抵达大理，古城漫步与苍山日落。落地后由平台安排车辆接站至洱海边客栈，午后沿复兴路慢走进大理古城，傍晚登苍山感通索道看日落。', img: IMG.dali },
      { day: 'DAY 02', title: '洱海环湖 · 喜洲', meet: '07:30 酒店大堂集合', copy: '环湖骑行，喜洲古镇与白族民居。清晨沿生态廊道骑行 18 公里，午餐在喜洲吃破酥粑粑，下午看严家大院的三坊一照壁。', img: IMG.erhai },
      { day: 'DAY 03', title: '丽江古城与束河', meet: '08:00 出发，车程约 2.5 小时', copy: '翻过洱源进入丽江坝子，午后在束河古镇的纳西院落喝茶，晚上自由逛四方街。', img: IMG.miao },
      { day: 'DAY 04', title: '玉龙雪山 · 蓝月谷', meet: '06:30 起床，07:00 出发', copy: '索道上至 4506 米观景台，午后下行至蓝月谷。山上温差大，平台提供羽绒服与氧气瓶。', img: IMG.daocheng },
      { day: 'DAY 05', title: '白沙古镇 · 玉湖村', meet: '09:00 出发', copy: '避开人流，去白沙看壁画与扎染作坊，下午在玉湖村的石头房子前喝一杯咖啡。', img: IMG.erhai },
      { day: 'DAY 06', title: '返程 · 送站', meet: '按航班时间分批送站', copy: '早餐后自由活动，平台按航班时间安排送站，行程结束。', img: IMG.dali }
    ],
    gallery: {
      hotel: [IMG.dali, IMG.erhai],
      food: [IMG.mushroom, IMG.miao],
      spot: [IMG.daocheng, IMG.dali, IMG.erhai]
    }
  },
  {
    id: 'r-daocheng', type: '自驾游', title: '川西环线 · 稻城亚丁自驾',
    subtitle: '7 天｜成都起止｜途经城市 · 餐厅 · 酒店结构化解说，不提供地图',
    days: 7, nights: 6, cover: IMG.daocheng,
    normalPrice: 328000, vipPrice: 298000,
    departCity: '成都', capacity: 12, sold: 7, rating: '4.8',
    gather: { time: '参团首日 08:00', place: '成都 · 武侯祠东侧停车场' },
    tags: ['打包价含住宿餐饮', '自驾专属', '无大巴无导游'],
    selfDrive: true,
    video: true,
    ways: ['成都 → 康定 → 新都桥', '新都桥 → 理塘 → 稻城', '稻城 → 亚丁村 → 香格里拉镇'],
    itinerary: [
      { day: 'DAY 01', title: '成都 → 康定 → 新都桥', meet: '08:00 成都集合', copy: '沿 318 国道西行，翻折多山进入新都桥，平台已订好摄影家走廊旁的客栈。', img: IMG.daocheng },
      { day: 'DAY 02', title: '新都桥 → 理塘 → 稻城', meet: '07:00 出发', copy: '穿越毛垭大草原，午后抵稻城，海拔抬升明显，建议放慢节奏。', img: IMG.daocheng },
      { day: 'DAY 03', title: '亚丁村 · 冲古寺 · 珍珠海', meet: '07:30 进景区', copy: '换乘景区车至扎灌崩，徒步至冲古寺与珍珠海，近距离看仙乃日。', img: IMG.erhai },
      { day: 'DAY 04', title: '洛绒牛场 · 牛奶海', meet: '06:30 出发', copy: '长线徒步，往返约 6 小时，海拔 4600 米，量力而行。', img: IMG.dali },
      { day: 'DAY 05', title: '稻城 → 雅江', meet: '09:00 出发', copy: '返程线路，途经海子山姊妹湖。', img: IMG.erhai },
      { day: 'DAY 06', title: '雅江 → 泸定 → 成都', meet: '08:00 出发', copy: '泸定桥短暂停留，晚间回到成都。', img: IMG.miao },
      { day: 'DAY 07', title: '成都散团', meet: '自行安排返程', copy: '行程结束，可自行延住。', img: IMG.dali }
    ],
    gallery: {
      hotel: [IMG.daocheng, IMG.miao],
      food: [IMG.mushroom],
      spot: [IMG.daocheng, IMG.dali]
    }
  },
  {
    id: 'r-miao', type: '组团游', title: '黔东南苗寨深度',
    subtitle: '5 天 4 晚｜凯里集合｜西江千户苗寨 · 雷公山 · 长桌宴',
    days: 5, nights: 4, cover: IMG.miao,
    normalPrice: 298000, vipPrice: 268000,
    departCity: '凯里', capacity: 20, sold: 11, rating: '4.7',
    gather: { time: '参团首日 10:30', place: '凯里南站 · 广场东侧' },
    tags: ['含长桌宴', '非遗体验', '零自费'],
    selfDrive: false,
    video: false,
    itinerary: [
      { day: 'DAY 01', title: '凯里接站 · 西江千户苗寨', meet: '参团首日 10:30 · 凯里南站', copy: '午后抵达西江，晚上看吊脚楼灯火沿山铺开，参加苗家迎宾酒。', img: IMG.miao },
      { day: 'DAY 02', title: '雷公山徒步与苗年长桌宴', meet: '08:00 集合', copy: '上午登雷公山看云海，晚上长桌宴配酸汤鱼。', img: IMG.miao },
      { day: 'DAY 03', title: '郎德上寨 · 银饰工坊', meet: '09:00 出发', copy: '看苗族银饰锻造全过程，可亲手打一枚银片。', img: IMG.erhai },
      { day: 'DAY 04', title: '镇远古镇 · 舞阳河', meet: '08:30 出发', copy: '沿舞阳河看古城夜色，宿河边客栈。', img: IMG.erhai },
      { day: 'DAY 05', title: '返程送站', meet: '按车次送站', copy: '早餐后送站，行程结束。', img: IMG.dali }
    ],
    gallery: {
      hotel: [IMG.miao],
      food: [IMG.miao, IMG.mushroom],
      spot: [IMG.miao, IMG.daocheng]
    }
  },
  {
    id: 'r-erhai', type: '自驾游', title: '洱海环湖 · 喜洲',
    subtitle: '4 天 3 晚｜大理起止｜环湖路线 · 餐厅住宿全部打包',
    days: 4, nights: 3, cover: IMG.erhai,
    normalPrice: 188000, vipPrice: 168000,
    departCity: '大理', capacity: 16, sold: 9, rating: '4.8',
    gather: { time: '参团首日 14:00', place: '大理 · 才村码头停车场' },
    tags: ['打包价含住宿餐饮', '自驾专属', '无大巴无导游'],
    selfDrive: true,
    video: false,
    ways: ['才村码头 → 环海东路 → 小普陀', '喜洲古镇 → 海舌公园', '双廊 → 南诏风情岛'],
    itinerary: [
      { day: 'DAY 01', title: '才村码头 · 环湖东岸', meet: '14:00 才村码头', copy: '沿环海东路向南，日落前抵小普陀。', img: IMG.erhai },
      { day: 'DAY 02', title: '喜洲古镇 · 海舌公园', meet: '09:00 出发', copy: '看白族民居与扎染，午后去海舌公园。', img: IMG.tiedye },
      { day: 'DAY 03', title: '双廊 · 南诏风情岛', meet: '09:30 出发', copy: '北岸环线，晚宿双廊。', img: IMG.dali },
      { day: 'DAY 04', title: '返程', meet: '自行安排', copy: '行程结束。', img: IMG.dali }
    ],
    gallery: {
      hotel: [IMG.erhai],
      food: [IMG.tiedye],
      spot: [IMG.erhai, IMG.dali]
    }
  }
]

export const vipProduct = {
  id: 'p-vip',
  type: '权益',
  title: 'AI 智能相册网站',
  kicker: 'VIP MEMBERSHIP',
  copy: '价值 3980 元的 AI 智能相册网站，升级 VIP 会员仅需 398 元',
  price: 39800,
  refPrice: 398000,
  benefits: ['线路与特产双价中的 VIP 价', '专属 AI 相册网站（独立域名）', '相册站入口：按参团批次查看旅行相册']
}

export const products = [
  {
    id: 'p-mushroom', type: '特产', title: '云南野生菌礼盒', cover: IMG.mushroom,
    normalPrice: 18800, vipPrice: 16800, origin: '云南 · 楚雄', sales: 126,
    spec: '500g 装（牛肝菌 + 松茸片）', desc: '雨季头茬采摘，低温慢烘 18 小时，回水后仍有山野气。'
  },
  {
    id: 'p-tiedye', type: '特产', title: '大理扎染方巾', cover: IMG.tiedye,
    normalPrice: 14800, vipPrice: 12800, origin: '云南 · 大理', sales: 84,
    spec: '90 × 90cm 板蓝根扎染', desc: '周城村手工扎花，板蓝根发酵染缸，每一块的冰裂纹都不同。'
  },
  {
    id: 'p-tea', type: '特产', title: '普洱古树茶饼', cover: IMG.tea,
    normalPrice: 31800, vipPrice: 29800, origin: '云南 · 勐海', sales: 52,
    spec: '357g 生茶 · 2019 年料', desc: '古树春料石磨压制，汤色杏黄，回甘慢而长。'
  },
  {
    id: 'p-flower', type: '特产', title: '丽江鲜花饼', cover: IMG.flower,
    normalPrice: 10800, vipPrice: 8800, origin: '云南 · 丽江', sales: 210,
    spec: '8 枚 / 盒 · 玫瑰馅', desc: '清晨现采食用玫瑰，酥皮 12 层，出炉当日发出。'
  }
]

export const orders = [
  {
    id: 'HY20261115001', userId: 'u-b', kind: '线路', itemId: 'r-dali', title: '滇西秘境 · 大理丽江',
    spec: '6 天 5 晚 · 组团游 · 1 人', amount: 268000,
    status: '待付款', createdAt: '2026-11-15 20:12', thumb: IMG.dali
  },
  {
    id: 'HY20261113002', userId: 'u-b', kind: '商品', itemId: 'p-mushroom', title: '云南野生菌礼盒',
    spec: '500g 装 · 1 件', amount: 16800,
    status: '待收货', createdAt: '2026-11-13 11:04', thumb: IMG.mushroom
  },
  {
    id: 'HY20260705003', userId: 'u-b', kind: '线路', itemId: 'r-daocheng', title: '川西环线自驾 7 天',
    spec: '7 天 · 自驾游 · 2 人', amount: 328000,
    status: '已完成', createdAt: '2026-07-05 09:31', thumb: IMG.daocheng
  }
]

export const commissions = [
  { id: 'CM001', orderId: 'HY20260522011', beneficiaryId: 'u-b', level: 'DIRECT', amount: 10000, status: '已入账', fromUserId: 'u-c1', createdAt: '2026-05-22 15:08' },
  { id: 'CM002', orderId: 'HY20260522011', beneficiaryId: 'u-a', level: 'INDIRECT', amount: 10000, status: '已入账', fromUserId: 'u-c1', createdAt: '2026-05-22 15:08' },
  { id: 'CM003', orderId: 'HY20260613021', beneficiaryId: 'u-b', level: 'DIRECT', amount: 10000, status: '已入账', fromUserId: 'u-c2', createdAt: '2026-06-13 10:42' },
  { id: 'CM004', orderId: 'HY20260613021', beneficiaryId: 'u-a', level: 'INDIRECT', amount: 10000, status: '已入账', fromUserId: 'u-c2', createdAt: '2026-06-13 10:42' },
  { id: 'CM005', orderId: 'HY20260705031', beneficiaryId: 'u-b', level: 'DIRECT', amount: 20000, status: '已入账', fromUserId: 'u-c3', createdAt: '2026-07-05 19:26' },
  { id: 'CM006', orderId: 'HY20260821041', beneficiaryId: 'u-c1', level: 'DIRECT', amount: 10000, status: '已入账', fromUserId: 'u-d1', createdAt: '2026-08-21 12:03' },
  { id: 'CM007', orderId: 'HY20260821041', beneficiaryId: 'u-b', level: 'INDIRECT', amount: 10000, status: '已入账', fromUserId: 'u-d1', createdAt: '2026-08-21 12:03' },
  /* 待发放 / 冻结样例（X-01：预留 amount=0 行样式，默认不展示零元行） */
  { id: 'CM008', orderId: 'HY20261115001', beneficiaryId: 'u-b', level: 'DIRECT', amount: 10000, status: '待发放', fromUserId: 'u-c3', createdAt: '2026-11-15 20:20' },
  { id: 'CM009', orderId: 'HY20261113002', beneficiaryId: 'u-b', level: 'INDIRECT', amount: 10000, status: '冻结', fromUserId: 'u-d1', createdAt: '2026-11-13 11:10' }
]

export const wallet = [
  { id: 'W001', userId: 'u-b', type: '佣金 · 直推', amount: 10000, counterparty: '陈一 升级 398', status: '已入账', createdAt: '2026-05-22 15:08' },
  { id: 'W002', userId: 'u-b', type: '佣金 · 直推', amount: 10000, counterparty: '何雨 升级 398', status: '已入账', createdAt: '2026-06-13 10:42' },
  { id: 'W003', userId: 'u-b', type: '佣金 · 直推', amount: 20000, counterparty: '郑途 升级 398', status: '已入账', createdAt: '2026-07-05 19:26' },
  { id: 'W004', userId: 'u-b', type: '佣金 · 间推', amount: 10000, counterparty: '白露 升级 398', status: '已入账', createdAt: '2026-08-21 12:03' },
  { id: 'W005', userId: 'u-b', type: '充值', amount: 100000, counterparty: '分公司代充', status: '已入账', createdAt: '2026-09-02 09:14' },
  { id: 'W006', userId: 'u-b', type: '提现', amount: 22000, fee: 1320, counterparty: '工商银行 · 尾号 8823', status: '审核中', createdAt: '2026-09-18 16:40' },
  { id: 'W007', userId: 'u-b', type: '互转转出', amount: 39800, counterparty: '林岚 138****0102', status: '已入账', createdAt: '2026-10-02 10:22' }
]

export const albums = [
  {
    id: 'a-2026-spring', season: '2026 · 春季', route: '滇西秘境 · 大理丽江', photos: 48, videos: 6,
    cover: IMG.dali,
    thumbs: [IMG.dali, IMG.erhai, IMG.miao, IMG.daocheng, IMG.dali, IMG.erhai, IMG.miao, IMG.daocheng]
  },
  {
    id: 'a-2026-summer', season: '2026 · 夏季', route: '川西环线 · 稻城亚丁', photos: 62, videos: 9,
    cover: IMG.daocheng,
    thumbs: [IMG.daocheng, IMG.dali, IMG.miao, IMG.erhai, IMG.daocheng, IMG.dali, IMG.erhai, IMG.miao]
  },
  {
    id: 'a-2026-autumn', season: '2026 · 秋季', route: '黔东南苗寨深度', photos: 35, videos: 4,
    cover: IMG.miao,
    thumbs: [IMG.miao, IMG.daocheng, IMG.dali, IMG.erhai, IMG.miao, IMG.daocheng, IMG.dali, IMG.erhai]
  }
]

export const regions = {
  云南省: {
    昆明市: ['五华区', '盘龙区', '官渡区', '西山区'],
    大理白族自治州: ['大理市', '洱源县', '剑川县'],
    丽江市: ['古城区', '玉龙纳西族自治县']
  },
  四川省: {
    成都市: ['锦江区', '武侯区', '青羊区', '高新区'],
    甘孜藏族自治州: ['稻城县', '康定市', '理塘县']
  },
  贵州省: {
    黔东南苗族侗族自治州: ['凯里市', '雷山县', '镇远县']
  },
  广东省: {
    广州市: ['天河区', '越秀区', '海珠区'],
    深圳市: ['南山区', '福田区', '宝安区']
  },
  浙江省: {
    杭州市: ['西湖区', '余杭区', '拱墅区']
  },
  北京市: {
    北京市: ['朝阳区', '海淀区', '东城区']
  },
  湖北省: {
    武汉市: ['洪山区', '江汉区', '武昌区', '江夏区'],
    宜昌市: ['西陵区', '夷陵区']
  }
}

/* 固定合规文案（配置注入 / 常驻可见） */
export const copy = {
  vip398: '价值 3980 元的 AI 智能相册网站，升级 VIP 会员仅需 398 元',
  twoLevel: '两级封顶、合法合规',
  commissionRule: '仅直推 / 间推两级',
  withdrawFee: '提现手续费 6%，按分计算向下取整到分',
  withdrawAdvice: '建议用互转或升级 VIP 消费，避免手续费与个人所得税',
  withdrawAudit: '提现将进入人工审核',
  rechargeLimit: '单笔充值不超过 5000 元，请拆分为多次充值',
  rechargeHelp: '单笔不超过 5000 元，超额需拆分',
  referrerRequired: '请填写推荐人码，无邀请码无法注册',
  referrerLocked: '推荐关系为佣金前置；扫码注册成功直达首页',
  referrerRequireTip: '推荐关系为必填项',
  vipExpire: '不续费不再享受 VIP 会员价，取价回落普通会员价；会员资格、团队位置与推荐网络关系保留',
  gapAlert: '计佣已中断：推荐关系缺失（referrer_missing），请补绑后重算',
  teamDef: '团队人数＝全部后代；个人人数＝仅直推（口径固定）',
  emptyRoutes: '暂无可报名的线路',
  emptyOrders: '还没有订单',
  emptyDirect: '还没有直推会员，分享海报邀请第一位吧',
  emptyIncome: '暂无收入明细',
  emptyCommission: '暂无佣金记录',
  emptyAlbum: '还没有旅行相册，参团后运营会为你上传'
}

export const CURRENT_USER_ID = 'u-b'

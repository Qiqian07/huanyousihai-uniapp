-- ============================================================================
-- 环游四海 V1.0 · 业务表 + 后台菜单（基于 RuoYi 3.9.2）
 -- 先执行 ruoyi-modern 自带的 ry_20260417.sql（RuoYi 基础表），再执行本脚本。
-- 金额一律以「分」的 BIGINT 存储（COMPLIANCE）。
-- ============================================================================

-- ----------------------------
-- 1. 会员表
-- ----------------------------
create table hys_member (
    member_id        bigint(20)   not null auto_increment  comment '会员ID',
    name             varchar(64)  not null                 comment '名称',
    phone            varchar(20)  not null                 comment '手机号（登录账号 / 推荐人码）',
    password         varchar(100) not null                 comment '密码（BCrypt）',
    region           varchar(128) default ''               comment '省/市/区（/ 分隔，需选到区级）',
    referrer_id      bigint(20)   default null             comment '推荐人会员ID（COMPLIANCE: 注册强制绑定）',
    direct_index     int(4)       default 1                comment '直推序号：是推荐人的第几个直推（注册时定死，用于拨比拆分）',
    level            varchar(16)  default 'NORMAL'         comment 'NORMAL 普通 / VIP',
    vip_expire_at    varchar(20)  default ''               comment 'VIP 到期日 yyyy-MM-dd',
    balance_cents    bigint(20)   default 0                comment '余额（分）',
    green_points     bigint(20)   default 0                comment '绿色积分',
    upgraded_at      varchar(20)  default ''               comment '升级时间',
    create_time      varchar(32)  default ''               comment '注册时间',
    del_flag         char(1)      default '0'              comment '0 正常 / 1 删除',
    primary key (member_id),
    unique key uk_hys_member_phone (phone)
) engine=innodb auto_increment=100 comment='环游四海会员表';

-- ----------------------------
-- 2. 旅游线路表
-- ----------------------------
create table hys_route (
    route_id            bigint(20)   not null auto_increment comment '线路ID',
    title               varchar(128) not null                comment '标题',
    cover               varchar(255) default ''              comment '封面图',
    video_url           varchar(255) default ''              comment '视频入口',
    days                int(4)       default 1               comment '行程天数',
    normal_price_cents  bigint(20)   not null                comment '普通价（分）',
    vip_price_cents     bigint(20)   not null                comment 'VIP价（分）',
    gather_time         varchar(64)  default ''              comment '集合时间',
    gather_place        varchar(128) default ''              comment '集合地点',
    itinerary           text                                 comment '行程票根 JSON：[{day,title,desc}]',
    status              char(1)      default '0'             comment '0 上架 / 1 下架',
    sort                int(4)       default 0               comment '排序',
    create_time         varchar(32)  default ''              comment '创建时间',
    primary key (route_id)
) engine=innodb auto_increment=100 comment='环游四海线路表';

-- ----------------------------
-- 3. 特产商品表
-- ----------------------------
create table hys_product (
    product_id          bigint(20)   not null auto_increment comment '商品ID',
    title               varchar(128) not null                comment '标题',
    cover               varchar(255) default ''              comment '封面图',
    spec                varchar(128) default ''              comment '规格',
    normal_price_cents  bigint(20)   not null                comment '普通价（分）',
    vip_price_cents     bigint(20)   not null                comment 'VIP价（分）',
    stock               int(8)       default 0               comment '库存',
    status              char(1)      default '0'             comment '0 上架 / 1 下架',
    sort                int(4)       default 0               comment '排序',
    create_time         varchar(32)  default ''              comment '创建时间',
    primary key (product_id)
) engine=innodb auto_increment=100 comment='环游四海特产商品表';

-- ----------------------------
-- 4. 订单表
-- ----------------------------
create table hys_order (
    order_id     varchar(32)  not null                comment '订单号 HYyyyyMMddNNNN',
    member_id    bigint(20)   not null                comment '会员ID',
    kind         varchar(20)  not null                comment 'ROUTE 线路 / PRODUCT 商品 / VIP_UPGRADE 升级 / VIP_RENEW 续费',
    item_id      bigint(20)   default 0               comment '线路/商品ID',
    title        varchar(128) not null                comment '标题',
    spec         varchar(128) default ''              comment '规格/集合信息',
    amount_cents bigint(20)   not null                comment '实付（分）',
    status       varchar(16)  not null                comment '待付款 / 待收货 / 已完成',
    thumb        varchar(255) default ''              comment '缩略图',
    create_time  varchar(32)  default ''              comment '下单时间',
    primary key (order_id),
    key idx_hys_order_member (member_id)
) engine=innodb comment='环游四海订单表';

-- ----------------------------
-- 5. 余额流水表
-- ----------------------------
create table hys_wallet_txn (
    txn_id       bigint(20)   not null auto_increment comment '流水ID',
    member_id    bigint(20)   not null                comment '会员ID',
    type         varchar(32)  not null                comment 'RECHARGE/TRANSFER_OUT/TRANSFER_IN/WITHDRAW/CONSUME/COMMISSION_DIRECT/COMMISSION_INDIRECT/VIP_UPGRADE/VIP_RENEW',
    amount_cents bigint(20)   not null                comment '变动金额（分，流入正/流出负）',
    fee_cents    bigint(20)   default 0               comment '手续费（分）',
    counterparty varchar(128) default ''              comment '对手方说明',
    status       varchar(16)  default '已入账'        comment '已入账 / 审核中 / 已退回',
    order_id     varchar(32)  default null            comment '关联单号（WD提现/RC充值/HY订单）',
    create_time  varchar(32)  default ''              comment '时间',
    primary key (txn_id),
    key idx_hys_txn_member (member_id)
) engine=innodb comment='环游四海余额流水表';

-- ----------------------------
-- 6. 充值申请表（后台审核后入账）
-- ----------------------------
create table hys_recharge (
    recharge_id  bigint(20)   not null auto_increment comment '申请ID',
    member_id    bigint(20)   not null                comment '会员ID',
    amount_cents bigint(20)   not null                comment '金额（分，≤500000）',
    status       varchar(16)  default 'AUDITING'      comment 'AUDITING/APPROVED/REJECTED',
    audit_by     bigint(20)   default null            comment '审核人（后台用户ID）',
    audit_time   varchar(32)  default ''              comment '审核时间',
    remark       varchar(255) default ''              comment '备注',
    create_time  varchar(32)  default ''              comment '申请时间',
    primary key (recharge_id)
) engine=innodb comment='环游四海充值申请表';

-- ----------------------------
-- 7. 提现申请表（6% 手续费按分向下取整，后台审核）
-- ----------------------------
create table hys_withdraw (
    withdraw_id  bigint(20)   not null auto_increment comment '申请ID',
    member_id    bigint(20)   not null                comment '会员ID',
    amount_cents bigint(20)   not null                comment '提现金额（分）',
    fee_cents    bigint(20)   not null                comment '手续费 6%（分，向下取整）',
    arrive_cents bigint(20)   not null                comment '到账（分）',
    bank_info    varchar(128) default ''              comment '收款账户',
    status       varchar(16)  default 'AUDITING'      comment 'AUDITING/APPROVED/REJECTED',
    audit_by     bigint(20)   default null            comment '审核人',
    audit_time   varchar(32)  default ''              comment '审核时间',
    remark       varchar(255) default ''              comment '备注',
    create_time  varchar(32)  default ''              comment '申请时间',
    primary key (withdraw_id)
) engine=innodb comment='环游四海提现申请表';

-- ----------------------------
-- 8. 佣金记录表
-- ----------------------------
create table hys_commission (
    commission_id  bigint(20)   not null auto_increment comment '佣金ID',
    order_id       varchar(32)  not null                comment '升级订单号',
    beneficiary_id bigint(20)   not null                comment '受益人会员ID',
    from_member_id bigint(20)   not null                comment '产生佣金的升级会员ID',
    level          varchar(16)  not null                comment 'DIRECT 直推 / INDIRECT 间推（COMPLIANCE: 仅两级）',
    amount_cents   bigint(20)   not null                comment '佣金（分）',
    status         varchar(16)  default 'GRANTED'       comment 'GRANTED 已发放 / PENDING 待发放 / FROZEN 冻结',
    create_time    varchar(32)  default ''              comment '时间',
    primary key (commission_id),
    key idx_hys_cm_beneficiary (beneficiary_id),
    -- COMPLIANCE: 同一订单同一受益人只能计佣一次（数据库级幂等，防并发重复入账）
    unique key uk_hys_cm_order_beneficiary (order_id, beneficiary_id)
) engine=innodb comment='环游四海佣金记录表';

-- ----------------------------
-- 9. 相册批次表
-- ----------------------------
create table hys_album (
    album_id    bigint(20)   not null auto_increment comment '批次ID',
    batch_title varchar(128) not null                comment '批次标题',
    trip_date   varchar(20)  default ''              comment '出行日期',
    cover       varchar(255) default ''              comment '封面',
    images      text                                 comment 'JSON：{"hotel":[...],"food":[...],"scenery":[...]}',
    create_time varchar(32)  default ''              comment '创建时间',
    primary key (album_id)
) engine=innodb comment='环游四海相册批次表';

-- ----------------------------
-- 10. 异常对账缺口表（计佣中断 → 补绑重算）
-- ----------------------------
create table hys_recon_gap (
    gap_id        bigint(20)   not null auto_increment comment '缺口ID',
    order_id      varchar(32)  not null                comment '升级订单号',
    member_id     bigint(20)   not null                comment '升级会员ID',
    reason        varchar(32)  default 'referrer_missing' comment '中断原因',
    amount_cents  bigint(20)   default 0               comment '缺口金额（分）',
    note          varchar(255) default ''              comment '说明',
    status        varchar(16)  default 'OPEN'          comment 'OPEN 待处理 / REPAIRED 已补绑重算',
    repaired_by   bigint(20)   default null            comment '处理人（后台用户ID）',
    repaired_time varchar(32)  default ''              comment '处理时间',
    create_time   varchar(32)  default ''              comment '产生时间',
    primary key (gap_id),
    key idx_hys_gap_order (order_id)
) engine=innodb comment='环游四海异常对账缺口表';

-- ----------------------------
-- 后台菜单（父目录 6000「环游四海管理」）
-- ----------------------------
delete from sys_menu where menu_id between 6000 and 6099;
insert into sys_menu values (6000, '环游四海管理', 0, 5, 'hys', null, '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', '2026-11-17', '', null, '环游四海业务目录');
insert into sys_menu values (6010, '会员管理', 6000, 1, 'member', 'hys/member/index', '', 1, 0, 'C', '0', '0', 'hys:member:list', 'peoples', 'admin', '2026-11-17', '', null, '');
insert into sys_menu values (6011, '充值审核', 6000, 2, 'recharge', 'hys/recharge/index', '', 1, 0, 'C', '0', '0', 'hys:recharge:list', 'money', 'admin', '2026-11-17', '', null, '');
insert into sys_menu values (6012, '提现审核', 6000, 3, 'withdraw', 'hys/withdraw/index', '', 1, 0, 'C', '0', '0', 'hys:withdraw:list', 'salary', 'admin', '2026-11-17', '', null, '');
insert into sys_menu values (6013, '线路上架', 6000, 4, 'route', 'hys/route/index', '', 1, 0, 'C', '0', '0', 'hys:route:list', 'guide', 'admin', '2026-11-17', '', null, '');
insert into sys_menu values (6014, '商品上架', 6000, 5, 'product', 'hys/product/index', '', 1, 0, 'C', '0', '0', 'hys:product:list', 'goods', 'admin', '2026-11-17', '', null, '');
insert into sys_menu values (6015, '相册管理', 6000, 6, 'album', 'hys/album/index', '', 1, 0, 'C', '0', '0', 'hys:album:list', 'upload', 'admin', '2026-11-17', '', null, '');
insert into sys_menu values (6016, '异常对账', 6000, 7, 'recon', 'hys/recon/index', '', 1, 0, 'C', '0', '0', 'hys:recon:list', 'bug', 'admin', '2026-11-17', '', null, '');

-- 按钮权限
insert into sys_menu values (6021, '充值审核操作', 6011, 1, '', '', '', 1, 0, 'F', '0', '0', 'hys:recharge:audit', '#', 'admin', '2026-11-17', '', null, '');
insert into sys_menu values (6022, '提现审核操作', 6012, 1, '', '', '', 1, 0, 'F', '0', '0', 'hys:withdraw:audit', '#', 'admin', '2026-11-17', '', null, '');
insert into sys_menu values (6023, '线路维护', 6013, 1, '', '', '', 1, 0, 'F', '0', '0', 'hys:route:edit', '#', 'admin', '2026-11-17', '', null, '');
insert into sys_menu values (6024, '商品维护', 6014, 1, '', '', '', 1, 0, 'F', '0', '0', 'hys:product:edit', '#', 'admin', '2026-11-17', '', null, '');
insert into sys_menu values (6025, '相册维护', 6015, 1, '', '', '', 1, 0, 'F', '0', '0', 'hys:album:edit', '#', 'admin', '2026-11-17', '', null, '');
insert into sys_menu values (6026, '补绑重算', 6016, 1, '', '', '', 1, 0, 'F', '0', '0', 'hys:recon:repair', '#', 'admin', '2026-11-17', '', null, '');

-- ----------------------------
-- 种子数据：演示推荐树（脱敏，演示密码统一 admin123）
-- 树：平台运营 → 苏芮 → 林岚 → 周野 → 陈一/何雨/郑途 → 白露
-- ----------------------------
insert into hys_member (member_id, name, phone, password, region, referrer_id, direct_index, level, vip_expire_at, balance_cents, create_time) values
(1, '平台运营', '13800000000', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '云南省/昆明市/五华区', null, 1, 'VIP', '2027-12-31', 0, '2026-01-01 09:00'),
(2, '苏芮',     '13800000001', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '云南省/昆明市/盘龙区', 1, 1, 'VIP', '2027-06-30', 156000, '2026-02-01 10:00'),
(3, '林岚',     '13800000002', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '云南省/大理州/大理市', 2, 1, 'VIP', '2026-12-01', 89200, '2026-03-01 11:00'),
(4, '周野',     '13800000003', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '四川省/成都市/锦江区', 3, 1, 'NORMAL', '', 256000, '2026-04-01 12:00'),
(5, '陈一',     '13800000004', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '四川省/成都市/青羊区', 4, 1, 'NORMAL', '', 12000, '2026-05-01 13:00'),
(6, '何雨',     '13800000005', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '四川省/绵阳市/涪城区', 4, 2, 'VIP', '2027-03-15', 45600, '2026-05-10 14:00'),
(7, '郑途',     '13800000006', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '重庆市/渝中区/解放碑', 4, 3, 'NORMAL', '', 500, '2026-06-01 15:00'),
(8, '白露',     '13800000007', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '重庆市/南岸区/南坪', 7, 1, 'NORMAL', '', 0, '2026-06-20 16:00');

-- 演示线路 / 商品 / 相册（价格为演示口径）
insert into hys_route (title, cover, days, normal_price_cents, vip_price_cents, gather_time, gather_place, itinerary, status, sort, create_time) values
('云南昆大丽 6 日双人游', 'https://dummyimage.com/800x500/2f6f5e/ffffff&text=Yunnan+6D', 6, 399900, 359900, '每周六 08:30', '昆明长水机场 T1', '[{"day":1,"title":"昆明集合","desc":"翠湖·陆军讲武堂"},{"day":2,"title":"大理","desc":"洱海环游·双廊"},{"day":3,"title":"大理","desc":"古城自由行"},{"day":4,"title":"丽江","desc":"玉龙雪山·蓝月谷"},{"day":5,"title":"丽江","desc":"束河古镇"},{"day":6,"title":"返程","desc":"送机"}]', '0', 1, '2026-07-01 09:00'),
('川西小环线自驾 5 日', 'https://dummyimage.com/800x500/8a4b2f/ffffff&text=Chuanxi+5D', 5, 299900, 269900, '每周五 07:30', '成都天府广场', '[{"day":1,"title":"成都→四姑娘山","desc":"猫鼻梁观景"},{"day":2,"title":"丹巴","desc":"甲居藏寨"},{"day":3,"title":"新都桥","desc":"摄影天堂"},{"day":4,"title":"康定","desc":"木格措"},{"day":5,"title":"返程","desc":"泸定桥"}]', '0', 2, '2026-07-02 09:00');

insert into hys_product (title, cover, spec, normal_price_cents, vip_price_cents, stock, status, sort, create_time) values
('云南野生菌礼盒', 'https://dummyimage.com/600x600/5e7d4b/ffffff&text=Mushroom', '800g 干菌拼装', 16800, 14900, 50, '0', 1, '2026-07-03 09:00'),
('鲜花饼 10 枚礼盒', 'https://dummyimage.com/600x600/c2707f/ffffff&text=Flower+Cake', '500g 经典玫瑰', 6800, 5800, 200, '0', 2, '2026-07-03 09:01'),
('普洱茶饼（生茶）', 'https://dummyimage.com/600x600/3d3a35/ffffff&text=Pu-erh+Tea', '357g/饼 2023 春茶', 23800, 21000, 80, '0', 3, '2026-07-03 09:02');

insert into hys_album (batch_title, trip_date, cover, images, create_time) values
('2026 国庆 · 昆大丽批次', '2026-10-01', 'https://dummyimage.com/800x500/2f6f5e/ffffff&text=Batch+CN01', '{"hotel":["https://dummyimage.com/600x400/4a6b8a/ffffff&text=Hotel+1","https://dummyimage.com/600x400/4a6b8a/ffffff&text=Hotel+2"],"food":["https://dummyimage.com/600x400/8a6b4a/ffffff&text=Food+1"],"scenery":["https://dummyimage.com/600x400/4a8a6b/ffffff&text=Scenery+1","https://dummyimage.com/600x400/4a8a6b/ffffff&text=Scenery+2"]}', '2026-10-08 18:00');

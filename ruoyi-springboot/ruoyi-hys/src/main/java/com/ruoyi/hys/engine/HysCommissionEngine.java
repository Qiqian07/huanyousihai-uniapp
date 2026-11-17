package com.ruoyi.hys.engine;

/* ============================================================================
 * 环游四海 V1.0 · 佣金（提成）算法引擎（服务端权威实现）
 * 口径来源：AGENT.md §6（2026-11-13 已冻结）
 * ---------------------------------------------------------------------------
 * 硬约束（COMPLIANCE）
 *   1. 分销层级只有两级：DIRECT 直推 + INDIRECT 间推；无第三级、不顺延、不改派。
 *   2. 每笔 398 升级订单拨比恒为 200.00 元。
 *   3. 直推序号 1、2 → 直推 100 + 间推 100；序号 ≥ 3 → 直推 200 + 间推 0。
 *   4. 提现手续费 6%，按分计算向下取整到分。
 *   5. 金额一律以「分」的 long 整数存储，禁止浮点数。
 * 本类为纯函数实现（无 Spring / DB 依赖），可直接单元测试；
 * 与小程序端 src/utils/commission.js 保持语义一致，服务端为权威口径。
 * ========================================================================== */
public final class HysCommissionEngine {

    /** COMPLIANCE: 两级分销是国家红线，写成常量而非配置 */
    public static final int MAX_DEPTH = 2;
    /** 分：每单拨比 200.00 元 */
    public static final long PAYOUT_TOTAL = 20000L;
    /** 分：398.00 元 */
    public static final long VIP_PRICE = 39800L;
    /** 分：99.00 元/年（Q-03 比例待定） */
    public static final long VIP_RENEW = 9900L;
    /** 分：单笔充值上限 5000.00 元 */
    public static final long RECHARGE_MAX = 500000L;
    /** COMPLIANCE: 提现手续费 6% */
    public static final int WITHDRAW_FEE_RATE = 6;

    /** 佣金层级枚举 */
    public static final String LEVEL_DIRECT = "DIRECT";
    public static final String LEVEL_INDIRECT = "INDIRECT";

    private HysCommissionEngine() {
    }

    /* ------------------------------------------------------------ 金额工具 */

    /** 元（double 表单输入）→ 分（long），四舍五入到分 */
    public static long toCents(double yuan) {
        return Math.round(yuan * 100);
    }

    /** 分 → 元字符串（两位小数，如 39800 → "398.00"） */
    public static String fmt(long cents) {
        long abs = Math.abs(cents);
        String s = String.format("%d.%02d", abs / 100, abs % 100);
        return cents < 0 ? "-" + s : s;
    }

    /* ------------------------------------------------------------ 提现计算 */

    /** 提现手续费：6%，按分向下取整（整数运算，3333 分 → 199 分） */
    public static long withdrawFee(long cents) {
        if (cents <= 0) {
            return 0L;
        }
        // COMPLIANCE: 整数运算，3333 × 6 ÷ 100 = 199.98 → 199 分
        return (cents * WITHDRAW_FEE_RATE) / 100;
    }

    /** 提现结算：手续费 + 到账金额 */
    public static WithdrawSettle withdrawSettle(long cents) {
        long fee = withdrawFee(cents);
        return new WithdrawSettle(cents, fee, cents - fee);
    }

    public record WithdrawSettle(long amount, long fee, long arrive) {
    }

    /* ------------------------------------------------- §6.1 拨比拆分（纯函数） */

    /**
     * 直推序号 → 拨比拆分。
     * 序号 1、2 → 直推 100 + 间推 100；序号 ≥ 3 → 直推 200 + 间推 0。
     *
     * @param directIndex 该新会员是其直推人的第几个直推（从 1 开始）
     * @return [directCents, indirectCents]
     */
    public static long[] payoutFor(int directIndex) {
        if (directIndex < 1) {
            throw new IllegalArgumentException("directIndex 必须为 >= 1 的整数，收到：" + directIndex);
        }
        if (directIndex <= 2) {
            return new long[] { 10000L, 10000L };
        }
        return new long[] { 20000L, 0L };
    }

    /* ---------------------------------------------------- 计佣主流程（升级 398） */

    /**
     * 一次 398 升级订单的两级计佣。
     * COMPLIANCE: 注册强制绑定推荐人 → 直推人、间推人必然存在；
     * 取不到即数据异常 → 中断计佣并产生对账缺口（禁止顺延 / 改派 / 静默少发）。
     */
    public static SettleRecord settleUpgrade(String orderId, long newUserId, int directIndex,
                                             Long referrerId, Long referrerOfReferrerId) {
        boolean hasReferrer = referrerId != null;
        boolean hasIndirect = referrerOfReferrerId != null;

        if (!hasReferrer || !hasIndirect) {
            long gapAmount = (hasReferrer && directIndex >= 1) ? payoutFor(directIndex)[1] : PAYOUT_TOTAL;
            return SettleRecord.aborted(orderId, newUserId, "referrer_missing", gapAmount,
                    hasReferrer ? "推荐关系缺失：直推人无上级" : "推荐关系缺失：订单会员无推荐人");
        }

        long[] split = payoutFor(directIndex);
        java.util.List<CommissionLine> lines = new java.util.ArrayList<>();
        lines.add(new CommissionLine(referrerId, LEVEL_DIRECT, 1, split[0], orderId));
        if (split[1] > 0) {
            lines.add(new CommissionLine(referrerOfReferrerId, LEVEL_INDIRECT, 2, split[1], orderId));
        }

        SettleRecord record = SettleRecord.ok(orderId, newUserId, directIndex, lines);
        assertRecord(record);
        return record;
    }

    /* -------------------------------------------------------------- 断言层 */

    /**
     * COMPLIANCE 断言：每单恰好拨满 200 元、层级 ≤ 2、层级枚举合法、受益人不重复。
     * 违反任一条抛出 IllegalStateException（调用方必须回滚事务）。
     */
    public static void assertRecord(SettleRecord r) {
        java.util.List<String> errs = new java.util.ArrayList<>();
        if (r.totalPaid() != PAYOUT_TOTAL) {
            errs.add("拨比异常：合计 " + r.totalPaid() + " 分 ≠ " + PAYOUT_TOTAL + " 分");
        }
        if (r.maxDepth() > MAX_DEPTH) {
            errs.add("层级异常：出现第 " + r.maxDepth() + " 级受益人");
        }
        for (CommissionLine l : r.lines()) {
            if (!LEVEL_DIRECT.equals(l.level()) && !LEVEL_INDIRECT.equals(l.level())) {
                errs.add("层级枚举非法：" + l.level());
            }
        }
        long distinct = r.lines().stream().map(CommissionLine::beneficiaryId).distinct().count();
        if (distinct != r.lines().size()) {
            errs.add("受益人重复");
        }
        if (!errs.isEmpty()) {
            throw new IllegalStateException("[COMPLIANCE] " + String.join("；", errs));
        }
    }

    /* ---------------------------------------------------------------- 校验 */

    /** 充值校验：单笔 ≤ 5000 元，超过返回建议拆分次数 */
    public static GuardResult checkRecharge(long cents) {
        if (cents <= 0) {
            return GuardResult.fail("请输入充值金额");
        }
        if (cents > RECHARGE_MAX) {
            return GuardResult.fail("RECHARGE_LIMIT", "单笔充值不超过 5000 元，请拆分为多次充值",
                    (cents + RECHARGE_MAX - 1) / RECHARGE_MAX);
        }
        return GuardResult.pass();
    }

    /** 提现校验：金额 > 0 且 ≤ 余额 */
    public static GuardResult checkWithdraw(long cents, long balance) {
        if (cents <= 0) {
            return GuardResult.fail("请输入提现金额");
        }
        if (cents > balance) {
            return GuardResult.fail("INSUFFICIENT", "余额不足", null);
        }
        return GuardResult.pass();
    }

    /** 互转校验：金额 > 0 且 ≤ 余额 */
    public static GuardResult checkTransfer(long cents, long balance) {
        if (cents <= 0) {
            return GuardResult.fail("请输入转账金额");
        }
        if (cents > balance) {
            return GuardResult.fail("INSUFFICIENT", "余额不足，可通过充值后完成互转", null);
        }
        return GuardResult.pass();
    }

    /* -------------------------------------------------------------- 结果模型 */

    /** 一条佣金受益行 */
    public record CommissionLine(long beneficiaryId, String level, int depth, long amount, String orderId) {
    }

    /** 一次计佣的完整结果（含对账缺口） */
    public record SettleRecord(boolean ok, boolean aborted, String orderId, long newUserId, int directIndex,
                               java.util.List<CommissionLine> lines, long totalPaid, int beneficiaryCount,
                               int maxDepth, Gap gap, String alert) {

        static SettleRecord ok(String orderId, long newUserId, int directIndex, java.util.List<CommissionLine> lines) {
            long total = lines.stream().mapToLong(CommissionLine::amount).sum();
            int maxDepth = lines.stream().mapToInt(CommissionLine::depth).max().orElse(0);
            return new SettleRecord(true, false, orderId, newUserId, directIndex,
                    java.util.List.copyOf(lines), total, lines.size(), maxDepth, null, null);
        }

        static SettleRecord aborted(String orderId, long newUserId, String reason, long amount, String note) {
            Gap gap = new Gap(orderId, newUserId, reason, amount, note);
            return new SettleRecord(false, true, orderId, newUserId, 0,
                    java.util.List.of(), 0L, 0, 0, gap,
                    "计佣已中断：推荐关系缺失（" + reason + "），请补绑后重算");
        }
    }

    /** 对账缺口：计佣中断的原因与涉及金额 */
    public record Gap(String orderId, long newUserId, String reason, long amount, String note) {
    }

    /** 校验结果 */
    public record GuardResult(boolean ok, String code, String message, Long suggestion) {
        static GuardResult pass() {
            return new GuardResult(true, null, null, null);
        }

        static GuardResult fail(String message) {
            return new GuardResult(false, null, message, null);
        }

        static GuardResult fail(String code, String message, Long suggestion) {
            return new GuardResult(false, code, message, suggestion);
        }
    }
}

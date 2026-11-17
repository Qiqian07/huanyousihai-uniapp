package com.ruoyi.hys.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ruoyi.hys.engine.HysCommissionEngine.GuardResult;
import com.ruoyi.hys.engine.HysCommissionEngine.SettleRecord;

/**
 * 佣金引擎单元测试（对齐 AGENT.md §8.2 合规用例 / §8.3 计算用例）
 * 全部为纯函数测试，不依赖数据库。
 */
class HysCommissionEngineTest {

    /* ------------------------------------------------ 拨比拆分 payoutFor */

    @Test
    @DisplayName("§8.3 我推荐第 1 人升级：直推 100 + 间推 100")
    void payoutIndex1() {
        long[] split = HysCommissionEngine.payoutFor(1);
        assertEquals(10000L, split[0]);
        assertEquals(10000L, split[1]);
    }

    @Test
    @DisplayName("§8.3 我推荐第 2 人升级：直推 100 + 间推 100")
    void payoutIndex2() {
        long[] split = HysCommissionEngine.payoutFor(2);
        assertEquals(10000L, split[0]);
        assertEquals(10000L, split[1]);
    }

    @Test
    @DisplayName("§8.3 我推荐第 3 人升级：直推 200 + 间推 0")
    void payoutIndex3() {
        long[] split = HysCommissionEngine.payoutFor(3);
        assertEquals(20000L, split[0]);
        assertEquals(0L, split[1]);
    }

    @Test
    @DisplayName("§8.3 我推荐第 9 人升级：仍是直推 200 + 间推 0")
    void payoutIndex9() {
        long[] split = HysCommissionEngine.payoutFor(9);
        assertEquals(20000L, split[0]);
        assertEquals(0L, split[1]);
    }

    @Test
    @DisplayName("payoutFor 非法序号抛出异常")
    void payoutInvalid() {
        assertThrows(Exception.class, () -> HysCommissionEngine.payoutFor(0));
        assertThrows(Exception.class, () -> HysCommissionEngine.payoutFor(-1));
    }

    /* ------------------------------------------------ 计佣主流程 settleUpgrade */

    @Test
    @DisplayName("§8.3 我第 1 个直推的下级再推荐一人：间推 100")
    void settleIndirectIndex1() {
        // 周野(4) 推荐 陈一(5)；陈一 的下级 何雨(6) 升级 → 周野直推 100，林岚(3) 间推 100
        SettleRecord r = HysCommissionEngine.settleUpgrade("HY202611170001", 6L, 1, 4L, 3L);
        assertTrue(r.ok());
        assertEquals(2, r.beneficiaryCount());
        assertEquals(20000L, r.totalPaid());
        assertEquals(2, r.maxDepth());
        assertEquals(10000L, r.lines().get(0).amount());
        assertEquals(10000L, r.lines().get(1).amount());
    }

    @Test
    @DisplayName("§8.3 我第 3 个直推的下级再推荐一人：直推 200、无间推")
    void settleIndirectIndex3() {
        SettleRecord r = HysCommissionEngine.settleUpgrade("HY202611170002", 8L, 3, 7L, 4L);
        assertTrue(r.ok());
        assertEquals(1, r.beneficiaryCount());
        assertEquals(20000L, r.totalPaid());
        assertEquals(20000L, r.lines().get(0).amount());
    }

    @Test
    @DisplayName("§8.2 A→B→C→D 四层链路：D 升级只惠及 C 与 B，A 不参与")
    void settleFourLayer() {
        SettleRecord r = HysCommissionEngine.settleUpgrade("HY202611170003", 4L, 1, 3L, 2L);
        assertTrue(r.ok());
        Set<Long> beneficiaries = new HashSet<>();
        r.lines().forEach(l -> beneficiaries.add(l.beneficiaryId()));
        assertTrue(beneficiaries.contains(3L), "C 应拿直推奖");
        assertTrue(beneficiaries.contains(2L), "B 应拿间推奖");
        assertFalse(beneficiaries.contains(1L), "A 不得参与（超过两级）");
        assertEquals(2, r.maxDepth());
    }

    @Test
    @DisplayName("§8.2 推荐关系缺失（无推荐人）→ 中断计佣，禁止顺延")
    void settleAbortedNoReferrer() {
        SettleRecord r = HysCommissionEngine.settleUpgrade("HY202611170004", 9L, 1, null, null);
        assertFalse(r.ok());
        assertTrue(r.aborted());
        assertEquals("referrer_missing", r.gap().reason());
        assertEquals(HysCommissionEngine.PAYOUT_TOTAL, r.gap().amount());
        assertEquals(0, r.lines().size());
    }

    @Test
    @DisplayName("§8.2 直推人无上级 → 中断计佣，缺口为间推份额 100")
    void settleAbortedNoIndirect() {
        SettleRecord r = HysCommissionEngine.settleUpgrade("HY202611170005", 9L, 1, 5L, null);
        assertFalse(r.ok());
        assertEquals("referrer_missing", r.gap().reason());
        assertEquals(10000L, r.gap().amount());
    }

    @Test
    @DisplayName("§8.3 直推序号 1~30：每单拨比恒 200 元且层级 ≤ 2")
    void settleAllIndexesCompliance() {
        for (int i = 1; i <= 30; i++) {
            SettleRecord r = HysCommissionEngine.settleUpgrade("HY20261117" + String.format("%03d", i),
                    100L + i, i, 4L, 3L);
            assertTrue(r.ok(), "index=" + i);
            assertEquals(HysCommissionEngine.PAYOUT_TOTAL, r.totalPaid(), "index=" + i);
            assertTrue(r.maxDepth() <= HysCommissionEngine.MAX_DEPTH, "index=" + i);
            assertNull(r.gap());
        }
    }

    /* ------------------------------------------------ COMPLIANCE 断言层 */

    @Test
    @DisplayName("§8.2 篡改合计 ≠ 200 元 → COMPLIANCE 拦截")
    void assertTamperedTotal() {
        SettleRecord r = HysCommissionEngine.settleUpgrade("HY202611170006", 9L, 1, 5L, 3L);
        SettleRecord tampered = new SettleRecord(true, false, r.orderId(), r.newUserId(), r.directIndex(),
                r.lines(), 19999L, r.beneficiaryCount(), r.maxDepth(), null, null);
        assertThrows(IllegalStateException.class, () -> HysCommissionEngine.assertRecord(tampered));
    }

    @Test
    @DisplayName("§8.2 伪造第三级受益人 → COMPLIANCE 拦截")
    void assertFakeThirdLevel() {
        SettleRecord r = HysCommissionEngine.settleUpgrade("HY202611170007", 9L, 1, 5L, 3L);
        SettleRecord forged = new SettleRecord(true, false, r.orderId(), r.newUserId(), r.directIndex(),
                java.util.List.of(
                        new HysCommissionEngine.CommissionLine(5L, "DIRECT", 1, 10000L, r.orderId()),
                        new HysCommissionEngine.CommissionLine(3L, "LEVEL3", 3, 10000L, r.orderId())),
                20000L, 2, 3, null, null);
        assertThrows(IllegalStateException.class, () -> HysCommissionEngine.assertRecord(forged));
    }

    @Test
    @DisplayName("§8.2 同一人同时拿直推与间推 → COMPLIANCE 拦截")
    void assertDuplicateBeneficiary() {
        SettleRecord forged = new SettleRecord(true, false, "HY202611170008", 9L, 1,
                java.util.List.of(
                        new HysCommissionEngine.CommissionLine(5L, "DIRECT", 1, 10000L, "HY202611170008"),
                        new HysCommissionEngine.CommissionLine(5L, "INDIRECT", 2, 10000L, "HY202611170008")),
                20000L, 2, 2, null, null);
        assertThrows(IllegalStateException.class, () -> HysCommissionEngine.assertRecord(forged));
    }

    /* ------------------------------------------------ 提现 6% 手续费 */

    @Test
    @DisplayName("§8.3 提现 1000 元 → 手续费 60 元，到账 940 元")
    void withdrawFee1000() {
        HysCommissionEngine.WithdrawSettle s = HysCommissionEngine.withdrawSettle(100000L);
        assertEquals(6000L, s.fee());
        assertEquals(94000L, s.arrive());
    }

    @Test
    @DisplayName("§8.3 提现 33.33 元 → 手续费 1.99 元（向下取整），到账 31.34 元")
    void withdrawFee3333() {
        assertEquals(199L, HysCommissionEngine.withdrawFee(3333L));
        HysCommissionEngine.WithdrawSettle s = HysCommissionEngine.withdrawSettle(3333L);
        assertEquals(3134L, s.arrive());
    }

    @Test
    @DisplayName("提现 0 / 负数 → 手续费 0")
    void withdrawFeeZero() {
        assertEquals(0L, HysCommissionEngine.withdrawFee(0L));
        assertEquals(0L, HysCommissionEngine.withdrawFee(-100L));
    }

    /* ------------------------------------------------ 充值 / 互转 / 提现校验 */

    @Test
    @DisplayName("§8.3 充值 5001 元 → 拒绝并建议拆分 2 次")
    void rechargeOverLimit() {
        GuardResult r = HysCommissionEngine.checkRecharge(500100L);
        assertFalse(r.ok());
        assertEquals("RECHARGE_LIMIT", r.code());
        assertEquals(2L, r.suggestion());
    }

    @Test
    @DisplayName("§8.3 充值 5000 元 → 通过")
    void rechargeAtLimit() {
        assertTrue(HysCommissionEngine.checkRecharge(500000L).ok());
    }

    @Test
    @DisplayName("§8.3 充值 1 万元拆两次：每次 5000 元均通过")
    void rechargeSplit() {
        assertTrue(HysCommissionEngine.checkRecharge(500000L).ok());
        assertTrue(HysCommissionEngine.checkRecharge(500000L).ok());
        assertFalse(HysCommissionEngine.checkRecharge(1000000L).ok());
    }

    @Test
    @DisplayName("充值 0 / 负数 → 拒绝")
    void rechargeInvalid() {
        assertFalse(HysCommissionEngine.checkRecharge(0L).ok());
        assertFalse(HysCommissionEngine.checkRecharge(-1L).ok());
    }

    @Test
    @DisplayName("提现超过余额 → INSUFFICIENT")
    void withdrawInsufficient() {
        GuardResult r = HysCommissionEngine.checkWithdraw(1000L, 999L);
        assertFalse(r.ok());
        assertEquals("INSUFFICIENT", r.code());
    }

    @Test
    @DisplayName("互转超过余额 → INSUFFICIENT（提示充值后完成互转）")
    void transferInsufficient() {
        GuardResult r = HysCommissionEngine.checkTransfer(1000L, 999L);
        assertFalse(r.ok());
        assertTrue(r.message().contains("互转"));
    }

    /* ------------------------------------------------ 金额换算 */

    @Test
    @DisplayName("元 → 分：398 → 39800 分（四舍五入防浮点误差）")
    void toCents() {
        assertEquals(39800L, HysCommissionEngine.toCents(398.0));
        assertEquals(16800L, HysCommissionEngine.toCents(168.0));
        assertEquals(3333L, HysCommissionEngine.toCents(33.33));
        /* 1.005*100 在 IEEE754 下 = 100.49999…，Java 与前端 JS 的 Math.round 均得 100（语义对齐用例） */
        assertEquals(100L, HysCommissionEngine.toCents(1.005));
    }

    @Test
    @DisplayName("分 → 元字符串格式化")
    void fmt() {
        assertEquals("398.00", HysCommissionEngine.fmt(39800L));
        assertEquals("-5.00", HysCommissionEngine.fmt(-500L));
        assertEquals("0.01", HysCommissionEngine.fmt(1L));
    }
}

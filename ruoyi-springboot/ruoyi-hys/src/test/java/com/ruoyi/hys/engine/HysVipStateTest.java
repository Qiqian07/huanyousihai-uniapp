package com.ruoyi.hys.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ruoyi.hys.domain.HysMember;
import com.ruoyi.hys.service.impl.HysMemberServiceImpl;

/**
 * VIP 状态与取价规则测试（Q-04：过期回落普通价）
 * vipState / isVipActive / priceFor 不依赖数据库，可用 null mapper 构造。
 */
class HysVipStateTest {

    private final HysMemberServiceImpl service = new HysMemberServiceImpl(null);

    private HysMember vip(String expireAt) {
        HysMember m = new HysMember();
        m.setLevel("VIP");
        m.setVipExpireAt(expireAt);
        return m;
    }

    @Test
    @DisplayName("Q-04 VIP 有效：取 VIP 价")
    void vipActivePrice() {
        String future = LocalDate.now().plusDays(60).format(DateTimeFormatter.ISO_LOCAL_DATE);
        assertTrue(service.isVipActive(vip(future)));
        assertEquals("active", service.vipState(vip(future)));
        assertEquals(359900L, service.priceFor(399900L, 359900L, vip(future)));
    }

    @Test
    @DisplayName("Q-04 VIP 即将到期（≤30 天）：仍有效但标记 soon")
    void vipSoon() {
        String soon = LocalDate.now().plusDays(20).format(DateTimeFormatter.ISO_LOCAL_DATE);
        assertTrue(service.isVipActive(vip(soon)));
        assertEquals("soon", service.vipState(vip(soon)));
    }

    @Test
    @DisplayName("Q-04 VIP 已过期：回落普通价")
    void vipExpiredPrice() {
        String past = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        assertFalse(service.isVipActive(vip(past)));
        assertEquals("expired", service.vipState(vip(past)));
        assertEquals(399900L, service.priceFor(399900L, 359900L, vip(past)));
    }

    @Test
    @DisplayName("普通会员：无 VIP 状态，取普通价")
    void normalMember() {
        HysMember m = new HysMember();
        m.setLevel("NORMAL");
        assertEquals("none", service.vipState(m));
        assertEquals(399900L, service.priceFor(399900L, 359900L, m));
    }
}

package com.ruoyi.hys.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.hys.domain.HysCommission;
import com.ruoyi.hys.domain.HysMember;
import com.ruoyi.hys.domain.HysOrder;
import com.ruoyi.hys.domain.HysReconGap;
import com.ruoyi.hys.domain.HysWalletTxn;
import com.ruoyi.hys.engine.HysCommissionEngine;
import com.ruoyi.hys.engine.HysCommissionEngine.CommissionLine;
import com.ruoyi.hys.engine.HysCommissionEngine.SettleRecord;
import com.ruoyi.hys.mapper.HysCommissionMapper;
import com.ruoyi.hys.mapper.HysMemberMapper;
import com.ruoyi.hys.mapper.HysOrderMapper;
import com.ruoyi.hys.mapper.HysReconGapMapper;
import com.ruoyi.hys.mapper.HysWalletTxnMapper;
import com.ruoyi.hys.service.IHysVipService;

/**
 * VIP 服务实现
 * COMPLIANCE: 升级 398 → 两级计佣，每单拨比恒 200 元，引擎断言失败回滚整个事务。
 */
@Service
public class HysVipServiceImpl implements IHysVipService {

    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter T = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final HysMemberMapper memberMapper;
    private final HysOrderMapper orderMapper;
    private final HysCommissionMapper commissionMapper;
    private final HysWalletTxnMapper txnMapper;
    private final HysReconGapMapper gapMapper;
    private final HysOrderIdGenerator orderIdGenerator;

    public HysVipServiceImpl(HysMemberMapper memberMapper, HysOrderMapper orderMapper,
                             HysCommissionMapper commissionMapper, HysWalletTxnMapper txnMapper,
                             HysReconGapMapper gapMapper, HysOrderIdGenerator orderIdGenerator) {
        this.memberMapper = memberMapper;
        this.orderMapper = orderMapper;
        this.commissionMapper = commissionMapper;
        this.txnMapper = txnMapper;
        this.gapMapper = gapMapper;
        this.orderIdGenerator = orderIdGenerator;
    }

    @Override
    @Transactional
    public Map<String, Object> upgrade(Long memberId) {
        HysMember member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new ServiceException("会员不存在");
        }
        if ("VIP".equals(member.getLevel())
                && (member.getVipExpireAt() == null || member.getVipExpireAt().isEmpty()
                    || LocalDate.parse(member.getVipExpireAt(), D).isAfter(LocalDate.now().minusDays(1)))) {
            throw new ServiceException("您已是 VIP 会员，可走 99 元/年续费");
        }
        /* 1. 扣款 398（原子） */
        if (memberMapper.deductBalance(memberId, HysCommissionEngine.VIP_PRICE) == 0) {
            throw new ServiceException("余额不足，可通过互转或充值后完成升级");
        }

        /* 2. 升级 + 生成升级订单 */
        LocalDate expire = LocalDate.now().plusYears(1);
        HysMember update = new HysMember();
        update.setMemberId(memberId);
        update.setLevel("VIP");
        update.setVipExpireAt(expire.format(D));
        update.setUpgradedAt(LocalDateTime.now().format(T));
        memberMapper.updateMember(update);

        HysOrder order = new HysOrder();
        order.setOrderId(nextOrderId());
        order.setMemberId(memberId);
        order.setKind("VIP_UPGRADE");
        order.setItemId(0L);
        order.setTitle("AI 智能相册网站（升级 VIP）");
        order.setSpec("398 权益 · 自动升级 VIP 会员");
        order.setAmountCents(HysCommissionEngine.VIP_PRICE);
        order.setStatus("已完成");
        order.setCreateTime(LocalDateTime.now().format(T));
        orderMapper.insertOrder(order);

        /* 3. 两级计佣（直推序号在注册时已定死） */
        HysMember p1 = member.getReferrerId() == null ? null : memberMapper.selectById(member.getReferrerId());
        HysMember p2 = p1 == null ? null : (p1.getReferrerId() == null ? null : memberMapper.selectById(p1.getReferrerId()));
        int directIndex = member.getDirectIndex() == null ? 1 : member.getDirectIndex();

        SettleRecord record = HysCommissionEngine.settleUpgrade(order.getOrderId(), memberId, directIndex,
                p1 == null ? null : p1.getMemberId(), p2 == null ? null : p2.getMemberId());

        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("order", order);
        result.put("expireAt", expire.format(D));

        if (record.aborted()) {
            /* COMPLIANCE: 推荐关系缺失 → 中断计佣，产生对账缺口，禁止顺延/改派/静默少发 */
            HysReconGap gap = new HysReconGap();
            gap.setOrderId(order.getOrderId());
            gap.setMemberId(memberId);
            gap.setReason(record.gap().reason());
            gap.setAmountCents(record.gap().amount());
            gap.setNote(record.gap().note());
            gap.setStatus("OPEN");
            gap.setCreateTime(LocalDateTime.now().format(T));
            gapMapper.insertGap(gap);
            result.put("commission", "interrupted");
            result.put("alert", record.alert());
        } else {
            for (CommissionLine line : record.lines()) {
                HysCommission commission = new HysCommission();
                commission.setOrderId(order.getOrderId());
                commission.setBeneficiaryId(line.beneficiaryId());
                commission.setFromMemberId(memberId);
                commission.setLevel(line.level());
                commission.setAmountCents(line.amount());
                commission.setStatus("GRANTED");
                commission.setCreateTime(LocalDateTime.now().format(T));
                commissionMapper.insertCommission(commission);
                /* 佣金即时入受益人余额 */
                memberMapper.creditBalance(line.beneficiaryId(), line.amount());

                HysWalletTxn txn = new HysWalletTxn();
                txn.setMemberId(line.beneficiaryId());
                txn.setType("DIRECT".equals(line.level()) ? "COMMISSION_DIRECT" : "COMMISSION_INDIRECT");
                txn.setAmountCents(line.amount());
                txn.setCounterparty(member.getName() + " 升级 398");
                txn.setStatus("已入账");
                txn.setOrderId(order.getOrderId());
                txn.setCreateTime(LocalDateTime.now().format(T));
                txnMapper.insertTxn(txn);
            }
            result.put("commission", "granted");
            result.put("totalPaid", record.totalPaid());
            result.put("beneficiaryCount", record.beneficiaryCount());
        }

        /* 4. 自己的升级支出流水 */
        HysWalletTxn selfTxn = new HysWalletTxn();
        selfTxn.setMemberId(memberId);
        selfTxn.setType("VIP_UPGRADE");
        selfTxn.setAmountCents(-HysCommissionEngine.VIP_PRICE);
        selfTxn.setCounterparty("AI 智能相册网站");
        selfTxn.setStatus("已入账");
        selfTxn.setOrderId(order.getOrderId());
        selfTxn.setCreateTime(LocalDateTime.now().format(T));
        txnMapper.insertTxn(selfTxn);

        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> renew(Long memberId) {
        HysMember member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new ServiceException("会员不存在");
        }
        if (!"VIP".equals(member.getLevel())) {
            throw new ServiceException("尚未开通 VIP，请先升级");
        }
        if (memberMapper.deductBalance(memberId, HysCommissionEngine.VIP_RENEW) == 0) {
            throw new ServiceException("余额不足，可通过充值后完成续费");
        }
        /* 从当前到期时间（已过期则从今天）顺延一年 */
        LocalDate base = LocalDate.now();
        if (member.getVipExpireAt() != null && !member.getVipExpireAt().isEmpty()) {
            LocalDate current = LocalDate.parse(member.getVipExpireAt(), D);
            if (current.isAfter(base)) {
                base = current;
            }
        }
        LocalDate expire = base.plusYears(1);

        HysMember update = new HysMember();
        update.setMemberId(memberId);
        update.setVipExpireAt(expire.format(D));
        memberMapper.updateMember(update);

        HysOrder order = new HysOrder();
        order.setOrderId(nextOrderId());
        order.setMemberId(memberId);
        order.setKind("VIP_RENEW");
        order.setItemId(0L);
        order.setTitle("VIP 续费 1 年");
        order.setSpec("99 元/年");
        order.setAmountCents(HysCommissionEngine.VIP_RENEW);
        order.setStatus("已完成");
        order.setCreateTime(LocalDateTime.now().format(T));
        orderMapper.insertOrder(order);

        HysWalletTxn txn = new HysWalletTxn();
        txn.setMemberId(memberId);
        txn.setType("VIP_RENEW");
        txn.setAmountCents(-HysCommissionEngine.VIP_RENEW);
        txn.setCounterparty("VIP 续费 1 年");
        txn.setStatus("已入账");
        txn.setOrderId(order.getOrderId());
        txn.setCreateTime(LocalDateTime.now().format(T));
        txnMapper.insertTxn(txn);

        /* Q-03 未结论：99 元续费提成比例待定，当前不计佣（不静默发钱） */
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("expireAt", expire.format(D));
        result.put("alert", "Q-03 未结论：99 元续费提成比例待定，当前不计佣");
        return result;
    }

    private String nextOrderId() {
        return orderIdGenerator.next();
    }
}

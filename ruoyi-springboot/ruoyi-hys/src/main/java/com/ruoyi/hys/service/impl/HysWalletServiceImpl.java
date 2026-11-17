package com.ruoyi.hys.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.hys.domain.HysMember;
import com.ruoyi.hys.domain.HysRecharge;
import com.ruoyi.hys.domain.HysWalletTxn;
import com.ruoyi.hys.domain.HysWithdraw;
import com.ruoyi.hys.engine.HysCommissionEngine;
import com.ruoyi.hys.mapper.HysMemberMapper;
import com.ruoyi.hys.mapper.HysRechargeMapper;
import com.ruoyi.hys.mapper.HysWalletTxnMapper;
import com.ruoyi.hys.mapper.HysWithdrawMapper;
import com.ruoyi.hys.service.IHysWalletService;

/**
 * 钱包服务实现
 */
@Service
public class HysWalletServiceImpl implements IHysWalletService {

    private static final DateTimeFormatter T = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final HysMemberMapper memberMapper;
    private final HysWalletTxnMapper txnMapper;
    private final HysRechargeMapper rechargeMapper;
    private final HysWithdrawMapper withdrawMapper;
    private final HysMemberServiceImpl memberService;

    public HysWalletServiceImpl(HysMemberMapper memberMapper, HysWalletTxnMapper txnMapper,
                                HysRechargeMapper rechargeMapper, HysWithdrawMapper withdrawMapper,
                                HysMemberServiceImpl memberService) {
        this.memberMapper = memberMapper;
        this.txnMapper = txnMapper;
        this.rechargeMapper = rechargeMapper;
        this.withdrawMapper = withdrawMapper;
        this.memberService = memberService;
    }

    /* ------------------------------------------------------------ 充值 */

    @Override
    @Transactional
    public Map<String, Object> submitRecharge(Long memberId, long cents) {
        HysCommissionEngine.GuardResult check = HysCommissionEngine.checkRecharge(cents);
        if (!check.ok()) {
            Map<String, Object> result = new HashMap<>();
            result.put("ok", false);
            result.put("code", check.code());
            result.put("message", check.message());
            if (check.suggestion() != null) {
                result.put("suggestion", check.suggestion());
            }
            return result;
        }
        HysRecharge recharge = new HysRecharge();
        recharge.setMemberId(memberId);
        recharge.setAmountCents(cents);
        recharge.setStatus("AUDITING");
        recharge.setCreateTime(now());
        rechargeMapper.insertRecharge(recharge);

        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("rechargeId", recharge.getRechargeId());
        result.put("message", "充值申请已提交，审核通过后入账");
        return result;
    }

    /* ------------------------------------------------------------ 互转 */

    @Override
    @Transactional
    public Map<String, Object> transfer(Long memberId, String toPhone, long cents) {
        HysCommissionEngine.GuardResult check = HysCommissionEngine.checkTransfer(cents,
                memberMapper.selectById(memberId).getBalanceCents());
        if (!check.ok()) {
            throw new ServiceException(check.message());
        }
        HysMember target = memberMapper.selectByPhone(toPhone);
        if (target == null) {
            throw new ServiceException("未找到该会员账号");
        }
        if (target.getMemberId().equals(memberId)) {
            throw new ServiceException("不能转给自己");
        }
        HysMember me = memberMapper.selectById(memberId);
        /* 原子扣减：受影响 0 行即余额不足（防并发超扣） */
        if (memberMapper.deductBalance(memberId, cents) == 0) {
            throw new ServiceException("余额不足，可通过充值后完成互转");
        }
        memberMapper.creditBalance(target.getMemberId(), cents);

        String outParty = target.getName() + " " + maskPhone(target.getPhone());
        String inParty = me.getName() + " " + maskPhone(me.getPhone());
        insertTxn(memberId, "TRANSFER_OUT", -cents, outParty, "已入账", null, null);
        insertTxn(target.getMemberId(), "TRANSFER_IN", cents, inParty, "已入账", null, null);

        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("target", target.getName());
        return result;
    }

    /* ------------------------------------------------------------ 提现 */

    @Override
    @Transactional
    public Map<String, Object> withdraw(Long memberId, long cents) {
        HysMember me = memberMapper.selectById(memberId);
        HysCommissionEngine.GuardResult check = HysCommissionEngine.checkWithdraw(cents, me.getBalanceCents());
        if (!check.ok()) {
            throw new ServiceException(check.message());
        }
        /* COMPLIANCE: 6% 手续费按分向下取整 */
        HysCommissionEngine.WithdrawSettle settle = HysCommissionEngine.withdrawSettle(cents);
        /* 先扣款（原子），再落申请单 */
        if (memberMapper.deductBalance(memberId, cents) == 0) {
            throw new ServiceException("余额不足");
        }
        HysWithdraw withdraw = new HysWithdraw();
        withdraw.setMemberId(memberId);
        withdraw.setAmountCents(cents);
        withdraw.setFeeCents(settle.fee());
        withdraw.setArriveCents(settle.arrive());
        withdraw.setBankInfo("工商银行 · 尾号 8823");
        withdraw.setStatus("AUDITING");
        withdraw.setCreateTime(now());
        withdrawMapper.insertWithdraw(withdraw);

        HysWalletTxn txn = insertTxn(memberId, "WITHDRAW", -cents, withdraw.getBankInfo(), "审核中", "WD" + withdraw.getWithdrawId(), settle.fee());

        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("withdrawId", withdraw.getWithdrawId());
        result.put("amount", settle.amount());
        result.put("fee", settle.fee());
        result.put("arrive", settle.arrive());
        result.put("txnId", txn.getTxnId());
        return result;
    }

    /* ------------------------------------------------------------ 查询 */

    @Override
    public List<HysWalletTxn> txns(Long memberId) {
        return txnMapper.selectByMember(memberId);
    }

    @Override
    public List<Map<String, Object>> myRecharges(Long memberId) {
        List<HysRecharge> list = rechargeMapper.selectByMember(memberId);
        return list.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("rechargeId", r.getRechargeId());
            m.put("amountCents", r.getAmountCents());
            m.put("status", r.getStatus());
            m.put("createTime", r.getCreateTime());
            return m;
        }).toList();
    }

    /* ------------------------------------------------------------ 工具 */

    @Override
    public String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone == null ? "—" : phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    private HysWalletTxn insertTxn(Long memberId, String type, long amountCents, String counterparty,
                                   String status, String orderId, Long feeCents) {
        HysWalletTxn txn = new HysWalletTxn();
        txn.setMemberId(memberId);
        txn.setType(type);
        txn.setAmountCents(amountCents);
        txn.setFeeCents(feeCents);
        txn.setCounterparty(counterparty);
        txn.setStatus(status);
        txn.setOrderId(orderId);
        txn.setCreateTime(now());
        txnMapper.insertTxn(txn);
        return txn;
    }

    private String now() {
        return LocalDateTime.now().format(T);
    }
}

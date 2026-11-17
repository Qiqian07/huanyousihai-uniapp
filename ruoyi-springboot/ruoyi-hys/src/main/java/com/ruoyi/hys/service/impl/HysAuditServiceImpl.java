package com.ruoyi.hys.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.hys.domain.HysRecharge;
import com.ruoyi.hys.domain.HysWalletTxn;
import com.ruoyi.hys.domain.HysWithdraw;
import com.ruoyi.hys.mapper.HysRechargeMapper;
import com.ruoyi.hys.mapper.HysWalletTxnMapper;
import com.ruoyi.hys.mapper.HysWithdrawMapper;
import com.ruoyi.hys.service.IHysAuditService;

/**
 * 后台审核服务实现
 */
@Service
public class HysAuditServiceImpl implements IHysAuditService {

    private static final DateTimeFormatter T = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final HysRechargeMapper rechargeMapper;
    private final HysWithdrawMapper withdrawMapper;
    private final HysWalletTxnMapper txnMapper;
    private final com.ruoyi.hys.mapper.HysMemberMapper memberMapper;

    public HysAuditServiceImpl(HysRechargeMapper rechargeMapper, HysWithdrawMapper withdrawMapper,
                               HysWalletTxnMapper txnMapper, com.ruoyi.hys.mapper.HysMemberMapper memberMapper) {
        this.rechargeMapper = rechargeMapper;
        this.withdrawMapper = withdrawMapper;
        this.txnMapper = txnMapper;
        this.memberMapper = memberMapper;
    }

    /* ------------------------------------------------------------ 充值审核 */

    @Override
    public List<HysRecharge> rechargeList(String status) {
        HysRecharge query = new HysRecharge();
        query.setStatus(status);
        return rechargeMapper.selectList(query);
    }

    @Override
    @Transactional
    public void approveRecharge(Long rechargeId, Long adminUserId, String remark) {
        HysRecharge recharge = mustRecharge(rechargeId);
        HysRecharge update = baseRecharge(rechargeId, adminUserId, remark);
        update.setStatus("APPROVED");
        if (rechargeMapper.updateStatus(update) == 0) {
            throw new ServiceException("该申请已被处理，请刷新");
        }
        /* 入账 + 流水 */
        memberMapper.creditBalance(recharge.getMemberId(), recharge.getAmountCents());

        HysWalletTxn txn = new HysWalletTxn();
        txn.setMemberId(recharge.getMemberId());
        txn.setType("RECHARGE");
        txn.setAmountCents(recharge.getAmountCents());
        txn.setCounterparty("分公司代充");
        txn.setStatus("已入账");
        txn.setOrderId("RC" + rechargeId);
        txn.setCreateTime(LocalDateTime.now().format(T));
        txnMapper.insertTxn(txn);
    }

    @Override
    @Transactional
    public void rejectRecharge(Long rechargeId, Long adminUserId, String remark) {
        HysRecharge update = baseRecharge(rechargeId, adminUserId, remark);
        update.setStatus("REJECTED");
        if (rechargeMapper.updateStatus(update) == 0) {
            throw new ServiceException("该申请已被处理，请刷新");
        }
    }

    /* ------------------------------------------------------------ 提现审核 */

    @Override
    public List<HysWithdraw> withdrawList(String status) {
        HysWithdraw query = new HysWithdraw();
        query.setStatus(status);
        return withdrawMapper.selectList(query);
    }

    @Override
    @Transactional
    public void approveWithdraw(Long withdrawId, Long adminUserId, String remark) {
        mustWithdraw(withdrawId);
        HysWithdraw update = baseWithdraw(withdrawId, adminUserId, remark);
        update.setStatus("APPROVED");
        if (withdrawMapper.updateStatus(update) == 0) {
            throw new ServiceException("该申请已被处理，请刷新");
        }
        /* 打款完成：原「审核中」流水改为已入账（模拟打款） */
        markTxn("WD" + withdrawId, "已入账");
    }

    @Override
    @Transactional
    public void rejectWithdraw(Long withdrawId, Long adminUserId, String remark) {
        HysWithdraw withdraw = mustWithdraw(withdrawId);
        HysWithdraw update = baseWithdraw(withdrawId, adminUserId, remark);
        update.setStatus("REJECTED");
        if (withdrawMapper.updateStatus(update) == 0) {
            throw new ServiceException("该申请已被处理，请刷新");
        }
        /* 驳回：余额退回 */
        memberMapper.creditBalance(withdraw.getMemberId(), withdraw.getAmountCents());
        markTxn("WD" + withdrawId, "已退回");
    }

    /* ------------------------------------------------------------ 工具 */

    private HysRecharge mustRecharge(Long rechargeId) {
        HysRecharge recharge = rechargeMapper.selectById(rechargeId);
        if (recharge == null) {
            throw new ServiceException("充值申请不存在");
        }
        return recharge;
    }

    private HysWithdraw mustWithdraw(Long withdrawId) {
        HysWithdraw withdraw = withdrawMapper.selectById(withdrawId);
        if (withdraw == null) {
            throw new ServiceException("提现申请不存在");
        }
        return withdraw;
    }

    private HysRecharge baseRecharge(Long id, Long adminUserId, String remark) {
        HysRecharge update = new HysRecharge();
        update.setRechargeId(id);
        update.setAuditBy(adminUserId);
        update.setAuditTime(LocalDateTime.now().format(T));
        update.setRemark(remark);
        return update;
    }

    private HysWithdraw baseWithdraw(Long id, Long adminUserId, String remark) {
        HysWithdraw update = new HysWithdraw();
        update.setWithdrawId(id);
        update.setAuditBy(adminUserId);
        update.setAuditTime(LocalDateTime.now().format(T));
        update.setRemark(remark);
        return update;
    }

    private void markTxn(String orderId, String status) {
        HysWalletTxn query = new HysWalletTxn();
        query.setOrderId(orderId);
        for (HysWalletTxn txn : txnMapper.selectList(query)) {
            txnMapper.updateStatus(txn.getTxnId(), status);
        }
    }

}

package com.ruoyi.hys.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.hys.domain.HysCommission;
import com.ruoyi.hys.domain.HysMember;
import com.ruoyi.hys.domain.HysReconGap;
import com.ruoyi.hys.domain.HysWalletTxn;
import com.ruoyi.hys.engine.HysCommissionEngine;
import com.ruoyi.hys.engine.HysCommissionEngine.CommissionLine;
import com.ruoyi.hys.engine.HysCommissionEngine.SettleRecord;
import com.ruoyi.hys.mapper.HysCommissionMapper;
import com.ruoyi.hys.mapper.HysMemberMapper;
import com.ruoyi.hys.mapper.HysReconGapMapper;
import com.ruoyi.hys.mapper.HysWalletTxnMapper;
import com.ruoyi.hys.service.IHysReconService;

/**
 * 异常对账服务实现：补绑推荐人 → 重算两级佣金 → 关闭缺口
 */
@Service
public class HysReconServiceImpl implements IHysReconService {

    private static final DateTimeFormatter T = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final HysReconGapMapper gapMapper;
    private final HysMemberMapper memberMapper;
    private final HysCommissionMapper commissionMapper;
    private final HysWalletTxnMapper txnMapper;

    public HysReconServiceImpl(HysReconGapMapper gapMapper, HysMemberMapper memberMapper,
                               HysCommissionMapper commissionMapper, HysWalletTxnMapper txnMapper) {
        this.gapMapper = gapMapper;
        this.memberMapper = memberMapper;
        this.commissionMapper = commissionMapper;
        this.txnMapper = txnMapper;
    }

    @Override
    public List<HysReconGap> list(String status, String orderId) {
        HysReconGap query = new HysReconGap();
        query.setStatus(status);
        query.setOrderId(orderId);
        return gapMapper.selectList(query);
    }

    @Override
    @Transactional
    public HysReconGap repair(Long gapId, String referrerPhone, Long adminUserId) {
        HysReconGap gap = gapMapper.selectById(gapId);
        if (gap == null || !"OPEN".equals(gap.getStatus())) {
            throw new ServiceException("缺口不存在或已处理");
        }
        HysMember member = memberMapper.selectById(gap.getMemberId());
        if (member == null) {
            throw new ServiceException("缺口对应会员不存在");
        }
        HysMember referrer = memberMapper.selectByPhone(referrerPhone);
        if (referrer == null) {
            throw new ServiceException("推荐人不存在，请核对手机号");
        }
        if (referrer.getMemberId().equals(member.getMemberId())) {
            throw new ServiceException("不能绑定自己为推荐人");
        }
        /* 防环：沿新推荐人向上走，不能遇到被绑会员 */
        Long cursor = referrer.getReferrerId();
        int depth = 0;
        while (cursor != null && depth < 32) {
            if (cursor.equals(member.getMemberId())) {
                throw new ServiceException("不能绑定自己的下级为推荐人（会形成循环关系链）");
            }
            HysMember up = memberMapper.selectById(cursor);
            cursor = up == null ? null : up.getReferrerId();
            depth++;
        }

        /* 补绑 */
        HysMember bind = new HysMember();
        bind.setMemberId(member.getMemberId());
        bind.setReferrerId(referrer.getMemberId());
        bind.setDirectIndex(memberMapper.countDirects(referrer.getMemberId()) + 1);
        memberMapper.updateMember(bind);

        /* 重算：取补绑后的最新关系链 */
        HysMember latest = memberMapper.selectById(member.getMemberId());
        HysMember p1 = latest.getReferrerId() == null ? null : memberMapper.selectById(latest.getReferrerId());
        HysMember p2 = p1 == null ? null : (p1.getReferrerId() == null ? null : memberMapper.selectById(p1.getReferrerId()));
        int directIndex = bind.getDirectIndex();

        SettleRecord record = HysCommissionEngine.settleUpgrade(gap.getOrderId(), member.getMemberId(),
                directIndex, p1 == null ? null : p1.getMemberId(), p2 == null ? null : p2.getMemberId());

        if (record.aborted()) {
            throw new ServiceException("重算后推荐关系仍缺失（" + record.gap().reason() + "），缺口保持待处理");
        }
        /* COMPLIANCE: 若该订单已有佣金记录，引擎/查重会拦截重复计佣 */
        List<HysCommission> existed = commissionMapper.selectByOrderId(gap.getOrderId());
        if (!existed.isEmpty()) {
            throw new ServiceException("该订单已存在佣金记录，禁止重复计佣");
        }
        for (CommissionLine line : record.lines()) {
            HysCommission commission = new HysCommission();
            commission.setOrderId(gap.getOrderId());
            commission.setBeneficiaryId(line.beneficiaryId());
            commission.setFromMemberId(member.getMemberId());
            commission.setLevel(line.level());
            commission.setAmountCents(line.amount());
            commission.setStatus("GRANTED");
            commission.setCreateTime(LocalDateTime.now().format(T));
            commissionMapper.insertCommission(commission);
            memberMapper.creditBalance(line.beneficiaryId(), line.amount());

            HysWalletTxn txn = new HysWalletTxn();
            txn.setMemberId(line.beneficiaryId());
            txn.setType("DIRECT".equals(line.level()) ? "COMMISSION_DIRECT" : "COMMISSION_INDIRECT");
            txn.setAmountCents(line.amount());
            txn.setCounterparty(member.getName() + " 升级 398（补绑重算）");
            txn.setStatus("已入账");
            txn.setOrderId(gap.getOrderId());
            txn.setCreateTime(LocalDateTime.now().format(T));
            txnMapper.insertTxn(txn);
        }

        /* 关闭缺口 */
        HysReconGap update = new HysReconGap();
        update.setGapId(gapId);
        update.setStatus("REPAIRED");
        update.setRepairedBy(adminUserId);
        update.setRepairedTime(LocalDateTime.now().format(T));
        update.setNote("已补绑推荐人 " + referrer.getName() + " 并重算发放 " + record.totalPaid() + " 分");
        if (gapMapper.updateStatus(update) == 0) {
            throw new ServiceException("缺口已被其他管理员处理，请刷新");
        }
        return gapMapper.selectById(gapId);
    }
}

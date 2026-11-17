package com.ruoyi.hys.service;

import java.util.List;
import com.ruoyi.hys.domain.HysRecharge;
import com.ruoyi.hys.domain.HysWithdraw;

/**
 * 后台审核服务（充值 / 提现）
 */
public interface IHysAuditService {

    public List<HysRecharge> rechargeList(String status);

    /** 充值审核通过：余额入账 + 流水 */
    public void approveRecharge(Long rechargeId, Long adminUserId, String remark);

    public void rejectRecharge(Long rechargeId, Long adminUserId, String remark);

    public List<HysWithdraw> withdrawList(String status);

    /** 提现审核通过：标记打款完成 */
    public void approveWithdraw(Long withdrawId, Long adminUserId, String remark);

    /** 提现驳回：余额退回 */
    public void rejectWithdraw(Long withdrawId, Long adminUserId, String remark);
}

package com.ruoyi.hys.domain;

/**
 * 充值申请 hys_recharge（需后台审核后入账）
 */
public class HysRecharge {
    private Long rechargeId;
    private Long memberId;
    private Long amountCents;
    /** AUDITING 审核中 / APPROVED 已入账 / REJECTED 已拒绝 */
    private String status;
    private Long auditBy;
    private String auditTime;
    private String remark;
    private String createTime;

    /** 非持久化 */
    private String memberName;
    private String memberPhone;

    public Long getRechargeId() { return rechargeId; }
    public void setRechargeId(Long rechargeId) { this.rechargeId = rechargeId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getAmountCents() { return amountCents; }
    public void setAmountCents(Long amountCents) { this.amountCents = amountCents; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getAuditBy() { return auditBy; }
    public void setAuditBy(Long auditBy) { this.auditBy = auditBy; }
    public String getAuditTime() { return auditTime; }
    public void setAuditTime(String auditTime) { this.auditTime = auditTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getMemberPhone() { return memberPhone; }
    public void setMemberPhone(String memberPhone) { this.memberPhone = memberPhone; }
}

package com.ruoyi.hys.domain;

/**
 * 提现申请 hys_withdraw（6% 手续费按分向下取整，需人工审核）
 */
public class HysWithdraw {
    private Long withdrawId;
    private Long memberId;
    private Long amountCents;
    private Long feeCents;
    private Long arriveCents;
    /** 收款账户展示，如「工商银行 · 尾号 8823」 */
    private String bankInfo;
    /** AUDITING 审核中 / APPROVED 已打款 / REJECTED 已退回 */
    private String status;
    private Long auditBy;
    private String auditTime;
    private String remark;
    private String createTime;

    /** 非持久化 */
    private String memberName;
    private String memberPhone;

    public Long getWithdrawId() { return withdrawId; }
    public void setWithdrawId(Long withdrawId) { this.withdrawId = withdrawId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getAmountCents() { return amountCents; }
    public void setAmountCents(Long amountCents) { this.amountCents = amountCents; }
    public Long getFeeCents() { return feeCents; }
    public void setFeeCents(Long feeCents) { this.feeCents = feeCents; }
    public Long getArriveCents() { return arriveCents; }
    public void setArriveCents(Long arriveCents) { this.arriveCents = arriveCents; }
    public String getBankInfo() { return bankInfo; }
    public void setBankInfo(String bankInfo) { this.bankInfo = bankInfo; }
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

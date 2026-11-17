package com.ruoyi.hys.domain;

/**
 * 佣金记录 hys_commission
 */
public class HysCommission {
    private Long commissionId;
    private String orderId;
    private Long beneficiaryId;
    /** 产生这笔佣金的升级会员 */
    private Long fromMemberId;
    /** DIRECT 直推 / INDIRECT 间推 */
    private String level;
    private Long amountCents;
    /** GRANTED 已发放 / PENDING 待发放 / FROZEN 冻结 */
    private String status;
    private String createTime;

    /** 非持久化 */
    private String fromMemberName;

    public Long getCommissionId() { return commissionId; }
    public void setCommissionId(Long commissionId) { this.commissionId = commissionId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Long getBeneficiaryId() { return beneficiaryId; }
    public void setBeneficiaryId(Long beneficiaryId) { this.beneficiaryId = beneficiaryId; }
    public Long getFromMemberId() { return fromMemberId; }
    public void setFromMemberId(Long fromMemberId) { this.fromMemberId = fromMemberId; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Long getAmountCents() { return amountCents; }
    public void setAmountCents(Long amountCents) { this.amountCents = amountCents; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getFromMemberName() { return fromMemberName; }
    public void setFromMemberName(String fromMemberName) { this.fromMemberName = fromMemberName; }
}

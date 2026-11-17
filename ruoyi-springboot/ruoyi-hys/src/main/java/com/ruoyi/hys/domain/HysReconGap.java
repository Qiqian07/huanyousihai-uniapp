package com.ruoyi.hys.domain;

/**
 * 异常对账缺口 hys_recon_gap：计佣中断（推荐关系缺失）时产生，补绑后重算关闭
 */
public class HysReconGap {
    private Long gapId;
    private String orderId;
    private Long memberId;
    /** referrer_missing */
    private String reason;
    private Long amountCents;
    private String note;
    /** OPEN 待处理 / REPAIRED 已补绑重算 */
    private String status;
    private Long repairedBy;
    private String repairedTime;
    private String createTime;

    /** 非持久化 */
    private String memberName;
    private String memberPhone;
    private String repairedByName;

    public Long getGapId() { return gapId; }
    public void setGapId(Long gapId) { this.gapId = gapId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getAmountCents() { return amountCents; }
    public void setAmountCents(Long amountCents) { this.amountCents = amountCents; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getRepairedBy() { return repairedBy; }
    public void setRepairedBy(Long repairedBy) { this.repairedBy = repairedBy; }
    public String getRepairedTime() { return repairedTime; }
    public void setRepairedTime(String repairedTime) { this.repairedTime = repairedTime; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getMemberPhone() { return memberPhone; }
    public void setMemberPhone(String memberPhone) { this.memberPhone = memberPhone; }
    public String getRepairedByName() { return repairedByName; }
    public void setRepairedByName(String repairedByName) { this.repairedByName = repairedByName; }
}

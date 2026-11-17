package com.ruoyi.hys.domain;

/**
 * 余额流水 hys_wallet_txn
 */
public class HysWalletTxn {
    private Long txnId;
    private Long memberId;
    /** RECHARGE / TRANSFER_OUT / TRANSFER_IN / WITHDRAW / CONSUME /
     *  COMMISSION_DIRECT / COMMISSION_INDIRECT / VIP_UPGRADE / VIP_RENEW */
    private String type;
    private Long amountCents;
    private Long feeCents;
    private String counterparty;
    /** 已入账 / 审核中 / 已退回 */
    private String status;
    private String orderId;
    private String createTime;

    public Long getTxnId() { return txnId; }
    public void setTxnId(Long txnId) { this.txnId = txnId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getAmountCents() { return amountCents; }
    public void setAmountCents(Long amountCents) { this.amountCents = amountCents; }
    public Long getFeeCents() { return feeCents; }
    public void setFeeCents(Long feeCents) { this.feeCents = feeCents; }
    public String getCounterparty() { return counterparty; }
    public void setCounterparty(String counterparty) { this.counterparty = counterparty; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}

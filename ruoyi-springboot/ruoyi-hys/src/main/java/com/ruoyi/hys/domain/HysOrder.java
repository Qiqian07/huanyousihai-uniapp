package com.ruoyi.hys.domain;

/**
 * 订单 hys_order
 */
public class HysOrder {
    /** 订单号，形如 HY20261117001 */
    private String orderId;
    private Long memberId;
    /** ROUTE 线路 / PRODUCT 商品 / VIP_UPGRADE 升级 / VIP_RENEW 续费 */
    private String kind;
    private Long itemId;
    private String title;
    private String spec;
    private Long amountCents;
    /** 待付款 / 待收货 / 已完成 */
    private String status;
    private String thumb;
    private String createTime;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSpec() { return spec; }
    public void setSpec(String spec) { this.spec = spec; }
    public Long getAmountCents() { return amountCents; }
    public void setAmountCents(Long amountCents) { this.amountCents = amountCents; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getThumb() { return thumb; }
    public void setThumb(String thumb) { this.thumb = thumb; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}

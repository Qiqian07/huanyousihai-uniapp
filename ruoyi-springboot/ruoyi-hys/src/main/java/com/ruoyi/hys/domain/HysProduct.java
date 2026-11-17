package com.ruoyi.hys.domain;

/**
 * 特产商品 hys_product
 */
public class HysProduct {
    private Long productId;
    private String title;
    private String cover;
    private String spec;
    private Long normalPriceCents;
    private Long vipPriceCents;
    private Integer stock;
    /** 0 上架 / 1 下架 */
    private String status;
    private Integer sort;
    private String createTime;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
    public String getSpec() { return spec; }
    public void setSpec(String spec) { this.spec = spec; }
    public Long getNormalPriceCents() { return normalPriceCents; }
    public void setNormalPriceCents(Long normalPriceCents) { this.normalPriceCents = normalPriceCents; }
    public Long getVipPriceCents() { return vipPriceCents; }
    public void setVipPriceCents(Long vipPriceCents) { this.vipPriceCents = vipPriceCents; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}

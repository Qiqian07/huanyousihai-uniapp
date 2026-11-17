package com.ruoyi.hys.domain;

/**
 * 旅游线路 hys_route
 */
public class HysRoute {
    private Long routeId;
    private String title;
    private String cover;
    private String videoUrl;
    private Integer days;
    private Long normalPriceCents;
    private Long vipPriceCents;
    private String gatherTime;
    private String gatherPlace;
    /** 行程票根 JSON：[{day:1,title,desc}] */
    private String itinerary;
    /** 0 上架 / 1 下架 */
    private String status;
    private Integer sort;
    private String createTime;

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }
    public Long getNormalPriceCents() { return normalPriceCents; }
    public void setNormalPriceCents(Long normalPriceCents) { this.normalPriceCents = normalPriceCents; }
    public Long getVipPriceCents() { return vipPriceCents; }
    public void setVipPriceCents(Long vipPriceCents) { this.vipPriceCents = vipPriceCents; }
    public String getGatherTime() { return gatherTime; }
    public void setGatherTime(String gatherTime) { this.gatherTime = gatherTime; }
    public String getGatherPlace() { return gatherPlace; }
    public void setGatherPlace(String gatherPlace) { this.gatherPlace = gatherPlace; }
    public String getItinerary() { return itinerary; }
    public void setItinerary(String itinerary) { this.itinerary = itinerary; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}

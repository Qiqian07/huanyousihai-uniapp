package com.ruoyi.hys.domain;

/**
 * 相册批次 hys_album（P-15/P-16 相册站）
 */
public class HysAlbum {
    private Long albumId;
    private String batchTitle;
    private String tripDate;
    private String cover;
    /** JSON：{"hotel":[...],"food":[...],"scenery":[...]} */
    private String images;
    private String createTime;

    public Long getAlbumId() { return albumId; }
    public void setAlbumId(Long albumId) { this.albumId = albumId; }
    public String getBatchTitle() { return batchTitle; }
    public void setBatchTitle(String batchTitle) { this.batchTitle = batchTitle; }
    public String getTripDate() { return tripDate; }
    public void setTripDate(String tripDate) { this.tripDate = tripDate; }
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}

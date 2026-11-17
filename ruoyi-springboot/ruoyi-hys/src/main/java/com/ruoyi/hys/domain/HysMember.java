package com.ruoyi.hys.domain;

/**
 * 会员对象 hys_member
 * 金额一律以「分」的 long 整数存储（COMPLIANCE）。
 */
public class HysMember {
    private Long memberId;
    private String name;
    private String phone;
    private String password;
    /** 省/市/区，三段以 / 连接 */
    private String region;
    /** 推荐人会员ID（注册强制绑定，关系链不能断） */
    private Long referrerId;
    /** 直推序号：是该推荐人的第几个直推（从 1 开始），注册时定死，用于拨比拆分 */
    private Integer directIndex;
    /** NORMAL / VIP */
    private String level;
    private String vipExpireAt;
    private Long balanceCents;
    private Long greenPoints;
    private String upgradedAt;
    private String createTime;
    private String delFlag;

    /** 非持久化：展示用 */
    private String referrerName;
    private Integer teamCount;
    private Integer directCount;
    /** 非持久化：VIP 三态 active/soon/expired/none */
    private String vipState;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public Long getReferrerId() { return referrerId; }
    public void setReferrerId(Long referrerId) { this.referrerId = referrerId; }
    public Integer getDirectIndex() { return directIndex; }
    public void setDirectIndex(Integer directIndex) { this.directIndex = directIndex; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getVipExpireAt() { return vipExpireAt; }
    public void setVipExpireAt(String vipExpireAt) { this.vipExpireAt = vipExpireAt; }
    public Long getBalanceCents() { return balanceCents; }
    public void setBalanceCents(Long balanceCents) { this.balanceCents = balanceCents; }
    public Long getGreenPoints() { return greenPoints; }
    public void setGreenPoints(Long greenPoints) { this.greenPoints = greenPoints; }
    public String getUpgradedAt() { return upgradedAt; }
    public void setUpgradedAt(String upgradedAt) { this.upgradedAt = upgradedAt; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getReferrerName() { return referrerName; }
    public void setReferrerName(String referrerName) { this.referrerName = referrerName; }
    public Integer getTeamCount() { return teamCount; }
    public void setTeamCount(Integer teamCount) { this.teamCount = teamCount; }
    public Integer getDirectCount() { return directCount; }
    public void setDirectCount(Integer directCount) { this.directCount = directCount; }
    public String getVipState() { return vipState; }
    public void setVipState(String vipState) { this.vipState = vipState; }
}

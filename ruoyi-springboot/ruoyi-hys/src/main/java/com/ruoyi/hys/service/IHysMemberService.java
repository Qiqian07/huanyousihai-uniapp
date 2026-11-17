package com.ruoyi.hys.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.hys.domain.HysMember;

/**
 * 会员服务
 */
public interface IHysMemberService {

    /** 注册（推荐人码必填，缺失一律拒绝） */
    Map<String, Object> register(Map<String, String> form);

    /** 登录，成功返回 {token, member} */
    Map<String, Object> login(String phone, String password);

    /** 个人资料（含 VIP 三态） */
    Map<String, Object> profile(Long memberId);

    /** 团队统计：team 全部后代 / direct 直推 */
    Map<String, Object> teamStats(Long memberId);

    /** 直推名单（含各自团队统计） */
    List<HysMember> directList(Long memberId);

    /** 修改密码 */
    void changePassword(Long memberId, String oldPwd, String newPwd);

    /** 找回密码 */
    void resetPassword(String phone, String newPwd);

    /** VIP 是否有效（Q-04：过期回落普通价） */
    boolean isVipActive(HysMember member);

    /** VIP 三态：active / soon(≤30天) / expired / none */
    String vipState(HysMember member);

    /** 适用价：VIP 有效取 VIP 价，否则普通价 */
    long priceFor(long normalCents, long vipCents, HysMember member);
}

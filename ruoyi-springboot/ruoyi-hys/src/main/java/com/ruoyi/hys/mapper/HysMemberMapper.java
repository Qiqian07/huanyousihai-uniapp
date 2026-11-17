package com.ruoyi.hys.mapper;

import java.util.List;
import com.ruoyi.hys.domain.HysMember;

/**
 * 会员 数据层
 */
public interface HysMemberMapper {
    public HysMember selectByPhone(String phone);

    public HysMember selectById(Long memberId);

    public List<HysMember> selectList(HysMember member);

    public int insertMember(HysMember member);

    public int updateMember(HysMember member);

    /** 原子扣减：余额不足返回 0 行（防并发超扣） */
    public int deductBalance(Long memberId, long cents);

    /** 原子入账 */
    public int creditBalance(Long memberId, long cents);

    /** 直推人数 */
    public int countDirects(Long memberId);

    /** 直推名单（含各自团队统计） */
    public List<HysMember> selectDirects(Long memberId);

    /** 团队人数（全部后代，递归 CTE，不含自己） */
    public int countDescendants(Long memberId);

    /** 该会员是推荐人的第几个直推（按注册顺序） */
    public int directIndexOf(Long memberId);
}

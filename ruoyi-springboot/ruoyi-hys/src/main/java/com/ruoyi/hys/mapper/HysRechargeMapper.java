package com.ruoyi.hys.mapper;

import java.util.List;
import com.ruoyi.hys.domain.HysRecharge;

/**
 * 充值申请 数据层
 */
public interface HysRechargeMapper {
    public HysRecharge selectById(Long rechargeId);

    public List<HysRecharge> selectList(HysRecharge recharge);

    public List<HysRecharge> selectByMember(Long memberId);

    public int insertRecharge(HysRecharge recharge);

    public int updateStatus(HysRecharge recharge);
}

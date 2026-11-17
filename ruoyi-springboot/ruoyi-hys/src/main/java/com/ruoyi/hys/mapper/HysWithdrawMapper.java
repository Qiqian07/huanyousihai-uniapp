package com.ruoyi.hys.mapper;

import java.util.List;
import com.ruoyi.hys.domain.HysWithdraw;

/**
 * 提现申请 数据层
 */
public interface HysWithdrawMapper {
    public HysWithdraw selectById(Long withdrawId);

    public List<HysWithdraw> selectList(HysWithdraw withdraw);

    public List<HysWithdraw> selectByMember(Long memberId);

    public int insertWithdraw(HysWithdraw withdraw);

    public int updateStatus(HysWithdraw withdraw);
}

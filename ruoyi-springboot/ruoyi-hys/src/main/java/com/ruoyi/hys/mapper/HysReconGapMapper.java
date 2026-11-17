package com.ruoyi.hys.mapper;

import java.util.List;
import com.ruoyi.hys.domain.HysReconGap;

/**
 * 异常对账缺口 数据层
 */
public interface HysReconGapMapper {
    public HysReconGap selectById(Long gapId);

    public List<HysReconGap> selectList(HysReconGap gap);

    public HysReconGap selectByOrderId(String orderId);

    public int insertGap(HysReconGap gap);

    public int updateStatus(HysReconGap gap);
}

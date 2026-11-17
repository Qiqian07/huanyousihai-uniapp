package com.ruoyi.hys.mapper;

import java.util.List;
import com.ruoyi.hys.domain.HysOrder;

/**
 * 订单 数据层
 */
public interface HysOrderMapper {
    public HysOrder selectById(String orderId);

    public List<HysOrder> selectByMember(Long memberId, String status);

    public List<HysOrder> selectList(HysOrder order);

    public int countByDay(String day);

    public int insertOrder(HysOrder order);

    public int updateStatus(String orderId, String status);
}

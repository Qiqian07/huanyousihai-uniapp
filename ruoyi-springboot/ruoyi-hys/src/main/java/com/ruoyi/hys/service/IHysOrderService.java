package com.ruoyi.hys.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.hys.domain.HysOrder;

/**
 * 订单服务（线路 / 特产）
 */
public interface IHysOrderService {

    /** 下单：价格由服务端按 VIP 状态计算，客户端传金额一律忽略 */
    Map<String, Object> createOrder(Long memberId, String kind, Long itemId);

    /** 支付（余额扣款） */
    Map<String, Object> payOrder(Long memberId, String orderId);

    /** 我的订单 */
    List<HysOrder> myOrders(Long memberId, String status);
}

package com.ruoyi.hys.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.hys.domain.HysMember;
import com.ruoyi.hys.domain.HysOrder;
import com.ruoyi.hys.domain.HysRoute;
import com.ruoyi.hys.domain.HysProduct;
import com.ruoyi.hys.domain.HysWalletTxn;
import com.ruoyi.hys.mapper.HysMemberMapper;
import com.ruoyi.hys.mapper.HysOrderMapper;
import com.ruoyi.hys.mapper.HysProductMapper;
import com.ruoyi.hys.mapper.HysRouteMapper;
import com.ruoyi.hys.mapper.HysWalletTxnMapper;
import com.ruoyi.hys.service.IHysMemberService;
import com.ruoyi.hys.service.IHysOrderService;

/**
 * 订单服务实现：价格一律由服务端按 VIP 状态计算（客户端传金额忽略）
 */
@Service
public class HysOrderServiceImpl implements IHysOrderService {

    private static final DateTimeFormatter T = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final HysOrderMapper orderMapper;
    private final HysRouteMapper routeMapper;
    private final HysProductMapper productMapper;
    private final HysMemberMapper memberMapper;
    private final HysWalletTxnMapper txnMapper;
    private final IHysMemberService memberService;
    private final HysOrderIdGenerator orderIdGenerator;

    public HysOrderServiceImpl(HysOrderMapper orderMapper, HysRouteMapper routeMapper,
                               HysProductMapper productMapper, HysMemberMapper memberMapper,
                               HysWalletTxnMapper txnMapper, IHysMemberService memberService,
                               HysOrderIdGenerator orderIdGenerator) {
        this.orderMapper = orderMapper;
        this.routeMapper = routeMapper;
        this.productMapper = productMapper;
        this.memberMapper = memberMapper;
        this.txnMapper = txnMapper;
        this.memberService = memberService;
        this.orderIdGenerator = orderIdGenerator;
    }

    @Override
    @Transactional
    public Map<String, Object> createOrder(Long memberId, String kind, Long itemId) {
        HysMember member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new ServiceException("会员不存在");
        }

        HysOrder order = new HysOrder();
        order.setOrderId(nextOrderId());
        order.setMemberId(memberId);

        if ("ROUTE".equals(kind)) {
            HysRoute route = routeMapper.selectById(itemId);
            if (route == null || !"0".equals(route.getStatus())) {
                throw new ServiceException("线路不存在或已下架");
            }
            order.setKind("ROUTE");
            order.setItemId(route.getRouteId());
            order.setTitle(route.getTitle());
            order.setSpec("集合 " + route.getGatherTime() + " · " + route.getGatherPlace());
            /* Q-04：VIP 有效享 VIP 价，过期回落普通价 */
            order.setAmountCents(memberService.priceFor(route.getNormalPriceCents(), route.getVipPriceCents(), member));
            order.setThumb(route.getCover());
        } else if ("PRODUCT".equals(kind)) {
            HysProduct product = productMapper.selectById(itemId);
            if (product == null || !"0".equals(product.getStatus())) {
                throw new ServiceException("商品不存在或已下架");
            }
            if (productMapper.deductStock(product.getProductId(), 1) == 0) {
                throw new ServiceException("商品库存不足");
            }
            order.setKind("PRODUCT");
            order.setItemId(product.getProductId());
            order.setTitle(product.getTitle());
            order.setSpec(product.getSpec());
            order.setAmountCents(memberService.priceFor(product.getNormalPriceCents(), product.getVipPriceCents(), member));
            order.setThumb(product.getCover());
        } else {
            throw new ServiceException("不支持的订单类型");
        }

        order.setStatus("待付款");
        order.setCreateTime(now());
        orderMapper.insertOrder(order);

        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("order", order);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> payOrder(Long memberId, String orderId) {
        HysOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new ServiceException("只能支付自己的订单");
        }
        if (!"待付款".equals(order.getStatus())) {
            throw new ServiceException("订单状态不可变更");
        }
        /* 原子扣款 */
        if (memberMapper.deductBalance(memberId, order.getAmountCents()) == 0) {
            throw new ServiceException("余额不足");
        }
        orderMapper.updateStatus(orderId, "待收货");

        HysWalletTxn txn = new HysWalletTxn();
        txn.setMemberId(memberId);
        txn.setType("CONSUME");
        txn.setAmountCents(-order.getAmountCents());
        txn.setCounterparty(order.getTitle());
        txn.setStatus("已入账");
        txn.setOrderId(orderId);
        txn.setCreateTime(now());
        txnMapper.insertTxn(txn);

        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("order", orderMapper.selectById(orderId));
        return result;
    }

    @Override
    public List<HysOrder> myOrders(Long memberId, String status) {
        return orderMapper.selectByMember(memberId, status);
    }

    /**
     * 订单号：HY + yyyyMMdd + 当日序号（Redis 原子自增，见 HysOrderIdGenerator）
     */
    private String nextOrderId() {
        return orderIdGenerator.next();
    }

    private String now() {
        return LocalDateTime.now().format(T);
    }
}

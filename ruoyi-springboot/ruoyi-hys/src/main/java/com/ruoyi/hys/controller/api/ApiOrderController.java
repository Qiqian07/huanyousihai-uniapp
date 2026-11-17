package com.ruoyi.hys.controller.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.hys.security.HysMemberContext;
import com.ruoyi.hys.service.IHysOrderService;

/**
 * 小程序端 · 下单 / 支付
 * 客户端只传 kind + itemId，价格由服务端按 VIP 状态计算（防篡改）。
 */
@RestController
@RequestMapping("/api/hys/order")
public class ApiOrderController {

    private final IHysOrderService orderService;

    public ApiOrderController(IHysOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public AjaxResult create(@RequestBody Map<String, Object> body) {
        String kind = String.valueOf(body.get("kind"));
        Long itemId = Long.valueOf(String.valueOf(body.get("itemId")));
        return AjaxResult.success(orderService.createOrder(HysMemberContext.get(), kind, itemId));
    }

    @PostMapping("/pay")
    public AjaxResult pay(@RequestBody Map<String, String> body) {
        return AjaxResult.success(orderService.payOrder(HysMemberContext.get(), body.get("orderId")));
    }

    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false) String status) {
        return AjaxResult.success(orderService.myOrders(HysMemberContext.get(), status));
    }
}

package com.ruoyi.hys.controller.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.hys.engine.HysCommissionEngine;
import com.ruoyi.hys.security.HysMemberContext;
import com.ruoyi.hys.service.IHysWalletService;

/**
 * 小程序端 · 钱包（充值 / 互转 / 提现）
 * 金额一律由服务端按「元 → 分」换算（Math.round），客户端传分无效。
 */
@RestController
@RequestMapping("/api/hys/wallet")
public class ApiWalletController {

    private final IHysWalletService walletService;

    public ApiWalletController(IHysWalletService walletService) {
        this.walletService = walletService;
    }

    /** 元 → 分换算工具（表单输入两位小数金额） */
    private long cents(Map<String, Object> body) {
        Object yuan = body.get("amount");
        if (yuan == null) {
            throw new IllegalArgumentException("请输入金额");
        }
        return HysCommissionEngine.toCents(Double.parseDouble(String.valueOf(yuan)));
    }

    @PostMapping("/recharge")
    public AjaxResult recharge(@RequestBody Map<String, Object> body) {
        return AjaxResult.success(walletService.submitRecharge(HysMemberContext.get(), cents(body)));
    }

    @PostMapping("/transfer")
    public AjaxResult transfer(@RequestBody Map<String, Object> body) {
        return AjaxResult.success(walletService.transfer(HysMemberContext.get(),
                String.valueOf(body.get("phone")), cents(body)));
    }

    @PostMapping("/withdraw")
    public AjaxResult withdraw(@RequestBody Map<String, Object> body) {
        return AjaxResult.success(walletService.withdraw(HysMemberContext.get(), cents(body)));
    }

    /** 提现试算：金额 → 手续费 / 到账（前端展示计算式用） */
    @GetMapping("/withdrawPreview")
    public AjaxResult withdrawPreview(double amount) {
        long c = HysCommissionEngine.toCents(amount);
        HysCommissionEngine.WithdrawSettle settle = HysCommissionEngine.withdrawSettle(c);
        return AjaxResult.success(settle);
    }
}

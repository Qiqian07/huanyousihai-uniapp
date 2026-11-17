package com.ruoyi.hys.controller.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.hys.security.HysMemberContext;
import com.ruoyi.hys.service.IHysVipService;

/**
 * 小程序端 · VIP 升级（398 两级计佣）/ 续费（99/年，Q-03 不计佣）
 */
@RestController
@RequestMapping("/api/hys/vip")
public class ApiVipController {

    private final IHysVipService vipService;

    public ApiVipController(IHysVipService vipService) {
        this.vipService = vipService;
    }

    @PostMapping("/upgrade")
    public AjaxResult upgrade() {
        return AjaxResult.success(vipService.upgrade(HysMemberContext.get()));
    }

    @PostMapping("/renew")
    public AjaxResult renew() {
        return AjaxResult.success(vipService.renew(HysMemberContext.get()));
    }
}

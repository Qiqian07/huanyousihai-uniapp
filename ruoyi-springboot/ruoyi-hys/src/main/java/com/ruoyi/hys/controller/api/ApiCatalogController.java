package com.ruoyi.hys.controller.api;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.hys.engine.HysCommissionEngine;
import com.ruoyi.hys.mapper.HysAlbumMapper;
import com.ruoyi.hys.mapper.HysProductMapper;
import com.ruoyi.hys.mapper.HysRouteMapper;
import com.ruoyi.hys.security.HysMemberContext;
import com.ruoyi.hys.service.IHysMemberService;

/**
 * 小程序端 · 商城目录（线路 / 商品 / 相册）与全局配置
 */
@RestController
@RequestMapping("/api/hys/catalog")
public class ApiCatalogController {

    private final HysRouteMapper routeMapper;
    private final HysProductMapper productMapper;
    private final HysAlbumMapper albumMapper;
    private final IHysMemberService memberService;

    public ApiCatalogController(HysRouteMapper routeMapper, HysProductMapper productMapper,
                                HysAlbumMapper albumMapper, IHysMemberService memberService) {
        this.routeMapper = routeMapper;
        this.productMapper = productMapper;
        this.albumMapper = albumMapper;
        this.memberService = memberService;
    }

    @GetMapping("/routes")
    public AjaxResult routes() {
        return AjaxResult.success(routeMapper.selectOnline());
    }

    @GetMapping("/products")
    public AjaxResult products() {
        return AjaxResult.success(productMapper.selectOnline());
    }

    @GetMapping("/albums")
    public AjaxResult albums() {
        return AjaxResult.success(albumMapper.selectList(null));
    }

    /** 一次性拉取小程序首页所需的全部状态（登录后调用） */
    @GetMapping("/state")
    public AjaxResult state() {
        Long memberId = HysMemberContext.get();
        Map<String, Object> data = new HashMap<>();
        data.put("profile", memberService.profile(memberId));
        data.put("team", memberService.teamStats(memberId));
        data.put("directs", memberService.directList(memberId));
        data.put("routes", routeMapper.selectOnline());
        data.put("products", productMapper.selectOnline());
        data.put("albums", albumMapper.selectList(null));

        Map<String, Object> config = new HashMap<>();
        config.put("vipPrice", HysCommissionEngine.VIP_PRICE);
        config.put("vipRenew", HysCommissionEngine.VIP_RENEW);
        config.put("rechargeMax", HysCommissionEngine.RECHARGE_MAX);
        config.put("withdrawFeeRate", HysCommissionEngine.WITHDRAW_FEE_RATE);
        config.put("payoutTotal", HysCommissionEngine.PAYOUT_TOTAL);
        data.put("config", config);
        return AjaxResult.success(data);
    }
}

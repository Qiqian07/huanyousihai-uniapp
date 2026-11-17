package com.ruoyi.hys.controller.api;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.hys.domain.HysCommission;
import com.ruoyi.hys.domain.HysOrder;
import com.ruoyi.hys.mapper.HysCommissionMapper;
import com.ruoyi.hys.security.HysMemberContext;
import com.ruoyi.hys.service.IHysMemberService;
import com.ruoyi.hys.service.IHysOrderService;
import com.ruoyi.hys.service.IHysWalletService;

/**
 * 小程序端 · 会员信息 / 团队 / 佣金 / 订单查询
 */
@RestController
@RequestMapping("/api/hys")
public class ApiMemberController {

    private final IHysMemberService memberService;
    private final IHysWalletService walletService;
    private final IHysOrderService orderService;
    private final HysCommissionMapper commissionMapper;

    public ApiMemberController(IHysMemberService memberService, IHysWalletService walletService,
                               IHysOrderService orderService, HysCommissionMapper commissionMapper) {
        this.memberService = memberService;
        this.walletService = walletService;
        this.orderService = orderService;
        this.commissionMapper = commissionMapper;
    }

    @GetMapping("/member/profile")
    public AjaxResult profile() {
        return AjaxResult.success(memberService.profile(HysMemberContext.get()));
    }

    @GetMapping("/member/team")
    public AjaxResult team() {
        Map<String, Object> data = memberService.teamStats(HysMemberContext.get());
        data.put("directs", memberService.directList(HysMemberContext.get()));
        return AjaxResult.success(data);
    }

    @GetMapping("/member/wallet")
    public AjaxResult wallet() {
        return AjaxResult.success(walletService.txns(HysMemberContext.get()));
    }

    @GetMapping("/member/recharges")
    public AjaxResult recharges() {
        return AjaxResult.success(walletService.myRecharges(HysMemberContext.get()));
    }

    @GetMapping("/member/commissions")
    public AjaxResult commissions() {
        List<HysCommission> list = commissionMapper.selectByBeneficiary(HysMemberContext.get());
        return AjaxResult.success(list);
    }

    @GetMapping("/member/orders")
    public AjaxResult orders(@RequestParam(required = false) String status) {
        List<HysOrder> list = orderService.myOrders(HysMemberContext.get(), status);
        return AjaxResult.success(list);
    }

    @PostMapping("/member/changePassword")
    public AjaxResult changePassword(@RequestBody Map<String, String> form) {
        memberService.changePassword(HysMemberContext.get(), form.get("oldPassword"), form.get("newPassword"));
        return AjaxResult.success();
    }
}

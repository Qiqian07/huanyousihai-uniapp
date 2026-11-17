package com.ruoyi.hys.controller.api;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.hys.domain.HysMember;
import com.ruoyi.hys.security.HysTokenService;
import com.ruoyi.hys.service.IHysMemberService;

/**
 * 小程序端 · 注册 / 登录 / 找回密码（免登接口）
 */
@Anonymous
@RestController
@RequestMapping("/api/hys/auth")
public class ApiAuthController {

    private final IHysMemberService memberService;
    private final HysTokenService tokenService;

    public ApiAuthController(IHysMemberService memberService, HysTokenService tokenService) {
        this.memberService = memberService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public AjaxResult register(@RequestBody Map<String, String> form) {
        Map<String, Object> data = memberService.register(form);
        HysMember member = (HysMember) data.get("member");
        data.put("token", tokenService.createToken(member.getMemberId()));
        return AjaxResult.success(data);
    }

    @PostMapping("/login")
    public AjaxResult login(@RequestBody Map<String, String> form) {
        Map<String, Object> data = memberService.login(form.get("phone"), form.get("password"));
        HysMember member = (HysMember) data.get("member");
        data.put("token", tokenService.createToken(member.getMemberId()));
        return AjaxResult.success(data);
    }

    @PostMapping("/resetPassword")
    public AjaxResult resetPassword(@RequestBody Map<String, String> form) {
        memberService.resetPassword(form.get("phone"), form.get("password"));
        return AjaxResult.success();
    }
}

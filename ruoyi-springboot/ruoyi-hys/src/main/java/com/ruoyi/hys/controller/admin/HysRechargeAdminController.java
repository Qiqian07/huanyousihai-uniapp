package com.ruoyi.hys.controller.admin;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.hys.domain.HysRecharge;
import com.ruoyi.hys.service.IHysAuditService;

/**
 * 后台 · 充值审核
 */
@RestController
@RequestMapping("/hys/recharge")
public class HysRechargeAdminController extends BaseController {

    private final IHysAuditService auditService;

    public HysRechargeAdminController(IHysAuditService auditService) {
        this.auditService = auditService;
    }

    @PreAuthorize("@ss.hasPermi('hys:recharge:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(required = false) String status) {
        startPage();
        List<HysRecharge> list = auditService.rechargeList(status);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('hys:recharge:audit')")
    @PostMapping("/approve/{rechargeId}")
    public AjaxResult approve(@PathVariable Long rechargeId, @RequestBody(required = false) java.util.Map<String, String> body) {
        String remark = body == null ? null : body.get("remark");
        auditService.approveRecharge(rechargeId, SecurityUtils.getUserId(), remark);
        return AjaxResult.success("充值审核通过，已入账");
    }

    @PreAuthorize("@ss.hasPermi('hys:recharge:audit')")
    @PostMapping("/reject/{rechargeId}")
    public AjaxResult reject(@PathVariable Long rechargeId, @RequestBody(required = false) java.util.Map<String, String> body) {
        String remark = body == null ? null : body.get("remark");
        auditService.rejectRecharge(rechargeId, SecurityUtils.getUserId(), remark);
        return AjaxResult.success("已拒绝该充值申请");
    }
}

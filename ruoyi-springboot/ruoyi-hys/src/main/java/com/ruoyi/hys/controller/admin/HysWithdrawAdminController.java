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
import com.ruoyi.hys.domain.HysWithdraw;
import com.ruoyi.hys.service.IHysAuditService;

/**
 * 后台 · 提现审核
 */
@RestController
@RequestMapping("/hys/withdraw")
public class HysWithdrawAdminController extends BaseController {

    private final IHysAuditService auditService;

    public HysWithdrawAdminController(IHysAuditService auditService) {
        this.auditService = auditService;
    }

    @PreAuthorize("@ss.hasPermi('hys:withdraw:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(required = false) String status) {
        startPage();
        List<HysWithdraw> list = auditService.withdrawList(status);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('hys:withdraw:audit')")
    @PostMapping("/approve/{withdrawId}")
    public AjaxResult approve(@PathVariable Long withdrawId, @RequestBody(required = false) java.util.Map<String, String> body) {
        String remark = body == null ? null : body.get("remark");
        auditService.approveWithdraw(withdrawId, SecurityUtils.getUserId(), remark);
        return AjaxResult.success("提现审核通过，打款完成（模拟）");
    }

    @PreAuthorize("@ss.hasPermi('hys:withdraw:audit')")
    @PostMapping("/reject/{withdrawId}")
    public AjaxResult reject(@PathVariable Long withdrawId, @RequestBody(required = false) java.util.Map<String, String> body) {
        String remark = body == null ? null : body.get("remark");
        auditService.rejectWithdraw(withdrawId, SecurityUtils.getUserId(), remark);
        return AjaxResult.success("已驳回，余额退回会员账户");
    }
}

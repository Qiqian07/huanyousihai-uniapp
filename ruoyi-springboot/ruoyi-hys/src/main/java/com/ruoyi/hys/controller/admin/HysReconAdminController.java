package com.ruoyi.hys.controller.admin;

import java.util.List;
import java.util.Map;
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
import com.ruoyi.hys.domain.HysReconGap;
import com.ruoyi.hys.service.IHysReconService;

/**
 * 后台 · 异常对账（计佣缺口列表 + 补绑重算）
 */
@RestController
@RequestMapping("/hys/recon")
public class HysReconAdminController extends BaseController {

    private final IHysReconService reconService;

    public HysReconAdminController(IHysReconService reconService) {
        this.reconService = reconService;
    }

    @PreAuthorize("@ss.hasPermi('hys:recon:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(required = false) String status,
                              @RequestParam(required = false) String orderId) {
        startPage();
        List<HysReconGap> list = reconService.list(status, orderId);
        return getDataTable(list);
    }

    /** 补绑重算：body.referrerPhone = 新推荐人手机号（前端须二次确认） */
    @PreAuthorize("@ss.hasPermi('hys:recon:repair')")
    @PostMapping("/repair/{gapId}")
    public AjaxResult repair(@PathVariable Long gapId, @RequestBody Map<String, String> body) {
        HysReconGap gap = reconService.repair(gapId, body.get("referrerPhone"), SecurityUtils.getUserId());
        return AjaxResult.success("补绑重算完成", gap);
    }
}

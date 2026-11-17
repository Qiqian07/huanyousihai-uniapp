package com.ruoyi.hys.controller.admin;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.hys.domain.HysRoute;
import com.ruoyi.hys.mapper.HysRouteMapper;

/**
 * 后台 · 线路上架管理
 */
@RestController
@RequestMapping("/hys/route")
public class HysRouteAdminController extends BaseController {

    private final HysRouteMapper routeMapper;

    public HysRouteAdminController(HysRouteMapper routeMapper) {
        this.routeMapper = routeMapper;
    }

    @PreAuthorize("@ss.hasPermi('hys:route:list')")
    @GetMapping("/list")
    public TableDataInfo list(HysRoute route) {
        startPage();
        List<HysRoute> list = routeMapper.selectList(route);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('hys:route:edit')")
    @PostMapping
    public AjaxResult add(@RequestBody HysRoute route) {
        route.setCreateTime(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        return toAjax(routeMapper.insertRoute(route));
    }

    @PreAuthorize("@ss.hasPermi('hys:route:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody HysRoute route) {
        return toAjax(routeMapper.updateRoute(route));
    }

    @PreAuthorize("@ss.hasPermi('hys:route:edit')")
    @DeleteMapping("/{routeIds}")
    public AjaxResult remove(@PathVariable Long[] routeIds) {
        return toAjax(routeMapper.deleteRouteByIds(routeIds));
    }

    /** 上架/下架：status '0' 上架 '1' 下架 */
    @PreAuthorize("@ss.hasPermi('hys:route:edit')")
    @PostMapping("/status/{routeId}/{status}")
    public AjaxResult changeStatus(@PathVariable Long routeId, @PathVariable String status) {
        HysRoute route = new HysRoute();
        route.setRouteId(routeId);
        route.setStatus(status);
        return toAjax(routeMapper.updateRoute(route));
    }
}

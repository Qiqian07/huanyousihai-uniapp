package com.ruoyi.hys.controller.admin;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.hys.domain.HysMember;
import com.ruoyi.hys.mapper.HysMemberMapper;

/**
 * 后台 · 会员管理
 */
@RestController
@RequestMapping("/hys/member")
public class HysMemberAdminController extends BaseController {

    private final HysMemberMapper memberMapper;

    public HysMemberAdminController(HysMemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    @PreAuthorize("@ss.hasPermi('hys:member:list')")
    @GetMapping("/list")
    public TableDataInfo list(HysMember member) {
        startPage();
        List<HysMember> list = memberMapper.selectList(member);
        list.forEach(m -> m.setPassword(null));
        return getDataTable(list);
    }
}

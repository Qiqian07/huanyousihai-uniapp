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
import com.ruoyi.hys.domain.HysAlbum;
import com.ruoyi.hys.mapper.HysAlbumMapper;

/**
 * 后台 · 相册上传管理（P-15/P-16 相册站数据源）
 */
@RestController
@RequestMapping("/hys/album")
public class HysAlbumAdminController extends BaseController {

    private final HysAlbumMapper albumMapper;

    public HysAlbumAdminController(HysAlbumMapper albumMapper) {
        this.albumMapper = albumMapper;
    }

    @PreAuthorize("@ss.hasPermi('hys:album:list')")
    @GetMapping("/list")
    public TableDataInfo list(HysAlbum album) {
        startPage();
        List<HysAlbum> list = albumMapper.selectList(album);
        return getDataTable(list);
    }

    /** images: {"hotel":[...],"food":[...],"scenery":[...]}（图片 URL 可来自 /common/upload） */
    @PreAuthorize("@ss.hasPermi('hys:album:edit')")
    @PostMapping
    public AjaxResult add(@RequestBody HysAlbum album) {
        album.setCreateTime(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        return toAjax(albumMapper.insertAlbum(album));
    }

    @PreAuthorize("@ss.hasPermi('hys:album:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody HysAlbum album) {
        return toAjax(albumMapper.updateAlbum(album));
    }

    @PreAuthorize("@ss.hasPermi('hys:album:edit')")
    @DeleteMapping("/{albumIds}")
    public AjaxResult remove(@PathVariable Long[] albumIds) {
        return toAjax(albumMapper.deleteAlbumByIds(albumIds));
    }
}

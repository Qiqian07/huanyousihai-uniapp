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
import com.ruoyi.hys.domain.HysProduct;
import com.ruoyi.hys.mapper.HysProductMapper;

/**
 * 后台 · 特产商品上架管理
 */
@RestController
@RequestMapping("/hys/product")
public class HysProductAdminController extends BaseController {

    private final HysProductMapper productMapper;

    public HysProductAdminController(HysProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @PreAuthorize("@ss.hasPermi('hys:product:list')")
    @GetMapping("/list")
    public TableDataInfo list(HysProduct product) {
        startPage();
        List<HysProduct> list = productMapper.selectList(product);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('hys:product:edit')")
    @PostMapping
    public AjaxResult add(@RequestBody HysProduct product) {
        product.setCreateTime(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        return toAjax(productMapper.insertProduct(product));
    }

    @PreAuthorize("@ss.hasPermi('hys:product:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody HysProduct product) {
        return toAjax(productMapper.updateProduct(product));
    }

    @PreAuthorize("@ss.hasPermi('hys:product:edit')")
    @DeleteMapping("/{productIds}")
    public AjaxResult remove(@PathVariable Long[] productIds) {
        return toAjax(productMapper.deleteProductByIds(productIds));
    }

    @PreAuthorize("@ss.hasPermi('hys:product:edit')")
    @PostMapping("/status/{productId}/{status}")
    public AjaxResult changeStatus(@PathVariable Long productId, @PathVariable String status) {
        HysProduct product = new HysProduct();
        product.setProductId(productId);
        product.setStatus(status);
        return toAjax(productMapper.updateProduct(product));
    }
}

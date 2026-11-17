package com.ruoyi.hys.mapper;

import java.util.List;
import com.ruoyi.hys.domain.HysProduct;

/**
 * 特产商品 数据层
 */
public interface HysProductMapper {
    public HysProduct selectById(Long productId);

    public List<HysProduct> selectOnline();

    public List<HysProduct> selectList(HysProduct product);

    public int insertProduct(HysProduct product);

    public int updateProduct(HysProduct product);

    public int deleteProductByIds(Long[] productIds);

    /** 原子扣库存：不足返回 0 行 */
    public int deductStock(Long productId, int count);
}

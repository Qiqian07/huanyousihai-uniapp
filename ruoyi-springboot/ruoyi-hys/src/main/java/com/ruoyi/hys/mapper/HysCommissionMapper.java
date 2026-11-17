package com.ruoyi.hys.mapper;

import java.util.List;
import com.ruoyi.hys.domain.HysCommission;

/**
 * 佣金记录 数据层
 */
public interface HysCommissionMapper {
    public List<HysCommission> selectByBeneficiary(Long beneficiaryId);

    public List<HysCommission> selectList(HysCommission commission);

    public List<HysCommission> selectByOrderId(String orderId);

    public int insertCommission(HysCommission commission);

    public int updateStatus(Long commissionId, String status);
}

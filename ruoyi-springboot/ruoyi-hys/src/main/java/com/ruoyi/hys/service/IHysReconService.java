package com.ruoyi.hys.service;

import java.util.List;
import com.ruoyi.hys.domain.HysReconGap;

/**
 * 异常对账服务：计佣缺口列表 + 补绑重算
 */
public interface IHysReconService {

    public List<HysReconGap> list(String status, String orderId);

    /**
     * 补绑推荐人并重算该订单佣金（仅限 OPEN 缺口；重复计佣会被 COMPLIANCE 断言拦截）
     */
    public HysReconGap repair(Long gapId, String referrerPhone, Long adminUserId);
}

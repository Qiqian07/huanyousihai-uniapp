package com.ruoyi.hys.service;

import java.util.Map;

/**
 * VIP 服务（398 开通两级计佣 / 99 续费 Q-03 不计佣）
 */
public interface IHysVipService {

    /** 升级 VIP：扣 398 → 升级 → 两级计佣（推荐关系缺失则产生对账缺口） */
    Map<String, Object> upgrade(Long memberId);

    /** 续费 99 元/年（Q-03：默认不计佣） */
    Map<String, Object> renew(Long memberId);
}

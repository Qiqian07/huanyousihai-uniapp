package com.ruoyi.hys.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.hys.mapper.HysOrderMapper;

/**
 * 订单号生成器：HY + yyyyMMdd + 当日四位序号。
 * 并发安全：序号取 Redis 原子自增（当日 0 点过期）；
 * Redis 不可用时降级为当日计数，由 order_id 主键唯一约束兜底
 * （冲突时事务回滚，调用方报错，用户重试即可）。
 */
@Component
public class HysOrderIdGenerator {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RedisCache redisCache;
    private final HysOrderMapper orderMapper;

    public HysOrderIdGenerator(RedisCache redisCache, HysOrderMapper orderMapper) {
        this.redisCache = redisCache;
        this.orderMapper = orderMapper;
    }

    public String next() {
        String day = LocalDate.now().format(DAY);
        int seq;
        try {
            String key = "hys:order:seq:" + day;
            long v = redisCache.redisTemplate.opsForValue().increment(key);
            if (v == 1) {
                redisCache.expire(key, 86400);
            }
            seq = (int) v;
        } catch (Exception e) {
            seq = orderMapper.countByDay(day) + 1;
        }
        return "HY" + day + String.format("%04d", seq);
    }
}

package com.ruoyi.hys.security;

/**
 * 小程序会员请求上下文（ThreadLocal）
 */
public class HysMemberContext {
    private static final ThreadLocal<Long> HOLDER = new ThreadLocal<>();

    private HysMemberContext() {
    }

    public static void set(Long memberId) {
        HOLDER.set(memberId);
    }

    public static Long get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}

package com.ruoyi.hys.security;

import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 会员接口鉴权拦截器：Authorization: Bearer &lt;jwt&gt;
 * 放行 OPTIONS 预检；其余请求必须携带有效令牌。
 */
public class HysAuthInterceptor implements HandlerInterceptor {

    private final HysTokenService tokenService;

    public HysAuthInterceptor(HysTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            Long memberId = tokenService.parseToken(auth.substring(7));
            if (memberId != null) {
                HysMemberContext.set(memberId);
                return true;
            }
        }
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"未登录或登录已过期\"}");
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        HysMemberContext.clear();
    }
}

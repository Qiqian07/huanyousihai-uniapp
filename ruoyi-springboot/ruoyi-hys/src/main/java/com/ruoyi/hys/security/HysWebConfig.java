package com.ruoyi.hys.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 会员接口拦截配置：/api/hys/** 需要登录（auth 接口除外）。
 * 说明：小程序接口走自有 JWT 拦截器，与后台 Spring Security 体系互不干扰。
 */
@Configuration
public class HysWebConfig implements WebMvcConfigurer {

    private final HysTokenService tokenService;

    public HysWebConfig(HysTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HysAuthInterceptor(tokenService))
                .addPathPatterns("/api/hys/**")
                .excludePathPatterns("/api/hys/auth/**");
    }
}

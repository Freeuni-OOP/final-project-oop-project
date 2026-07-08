package com.quizwebsite.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers the auth / admin interceptors, mirroring the old servlet filters:
 *   AuthFilter  → AuthInterceptor on everything except the public auth pages + static assets
 *   AdminFilter → AdminInterceptor on /admin/**
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final AdminInterceptor adminInterceptor;

    public WebConfig(AuthInterceptor authInterceptor, AdminInterceptor adminInterceptor) {
        this.authInterceptor = authInterceptor;
        this.adminInterceptor = adminInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login", "/register", "/logout",
                        "/css/**", "/js/**", "/images/**", "/favicon.ico", "/error");

        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**");
    }
}

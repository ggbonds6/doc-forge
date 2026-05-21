package com.docforge.govdoc.auth;

import com.docforge.govdoc.common.ApiException;
import com.docforge.govdoc.config.AdminAuthProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final AdminAuthProperties properties;

    public AuthInterceptor(AdminAuthProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.equals("Bearer " + properties.token())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "未登录或令牌无效");
        }
        return true;
    }
}


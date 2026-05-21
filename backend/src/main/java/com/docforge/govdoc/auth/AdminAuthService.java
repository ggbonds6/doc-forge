package com.docforge.govdoc.auth;

import com.docforge.govdoc.common.ApiException;
import com.docforge.govdoc.config.AdminAuthProperties;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {
    private final AdminAuthProperties properties;

    public AdminAuthService(AdminAuthProperties properties) {
        this.properties = properties;
    }

    public LoginResponse login(LoginRequest request) {
        if (!properties.username().equals(request.username()) || !properties.password().equals(request.password())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "账号或密码错误");
        }
        return new LoginResponse(properties.token(), properties.username());
    }

    public record LoginRequest(
            @NotBlank(message = "用户名不能为空") String username,
            @NotBlank(message = "密码不能为空") String password
    ) {
    }

    public record LoginResponse(String token, String username) {
    }
}


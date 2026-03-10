// -*- coding: utf-8 -*-
package com.scenic.ticket.controller;

import com.scenic.ticket.common.Result;
import com.scenic.ticket.dto.LoginRequest;
import com.scenic.ticket.dto.LoginResponse;
import com.scenic.ticket.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success("退出成功");
    }

    @GetMapping("/info")
    public Result<LoginResponse> getUserInfo(Authentication authentication) {
        String username = authentication.getName();
        LoginResponse info = authService.getUserInfo(username);
        return Result.success(info);
    }
}

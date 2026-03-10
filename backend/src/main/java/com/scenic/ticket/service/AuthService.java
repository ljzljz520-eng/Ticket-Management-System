// -*- coding: utf-8 -*-
package com.scenic.ticket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scenic.ticket.common.BusinessException;
import com.scenic.ticket.dto.LoginRequest;
import com.scenic.ticket.dto.LoginResponse;
import com.scenic.ticket.entity.SysUser;
import com.scenic.ticket.mapper.SysUserMapper;
import com.scenic.ticket.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OperationLogService logService;

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, request.getUsername())
        );

        if (user == null) {
            log.warn("登录失败: 用户不存在 username={}", request.getUsername());
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            log.warn("登录失败: 用户已禁用 username={}", request.getUsername());
            throw new BusinessException(401, "账号已被禁用，请联系管理员");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("登录失败: 密码错误 username={}", request.getUsername());
            throw new BusinessException(401, "用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        log.info("用户登录成功: username={}, role={}", user.getUsername(), user.getRole());
        logService.saveLog(user.getId(), user.getUsername(), "用户登录", null, null);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRole(user.getRole());
        response.setAvatar(user.getAvatar());
        return response;
    }

    /**
     * 获取当前用户信息
     */
    public LoginResponse getUserInfo(String username) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        );

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRole(user.getRole());
        response.setAvatar(user.getAvatar());
        return response;
    }
}

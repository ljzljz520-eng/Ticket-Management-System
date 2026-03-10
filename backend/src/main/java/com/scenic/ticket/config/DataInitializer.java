// -*- coding: utf-8 -*-
package com.scenic.ticket.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scenic.ticket.entity.SysUser;
import com.scenic.ticket.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器 — 确保测试账号存在且密码正确
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initUser("admin", "admin123", "系统管理员", "13800000001", "ADMIN");
        initUser("user", "user123", "售票员张三", "13800000002", "USER");
        log.info("数据初始化完成: 测试账号已就绪");
    }

    private void initUser(String username, String rawPassword, String realName, String phone, String role) {
        SysUser existing = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );

        if (existing == null) {
            SysUser user = new SysUser();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRealName(realName);
            user.setPhone(phone);
            user.setRole(role);
            user.setStatus(1);
            userMapper.insert(user);
            log.info("初始化用户: username={}, role={}", username, role);
        } else {
            // 确保密码正确（每次启动时更新密码为标准值）
            String encoded = passwordEncoder.encode(rawPassword);
            if (!passwordEncoder.matches(rawPassword, existing.getPassword())) {
                existing.setPassword(encoded);
                userMapper.updateById(existing);
                log.info("更新用户密码: username={}", username);
            }
        }
    }
}

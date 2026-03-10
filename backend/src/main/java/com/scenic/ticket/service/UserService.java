// -*- coding: utf-8 -*-
package com.scenic.ticket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenic.ticket.common.BusinessException;
import com.scenic.ticket.dto.UserDTO;
import com.scenic.ticket.entity.SysUser;
import com.scenic.ticket.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final OperationLogService logService;

    /**
     * 分页查询用户
     */
    public IPage<SysUser> listUsers(int page, int size, String username, String role, Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        if (username != null && !username.isEmpty()) {
            wrapper.like(SysUser::getUsername, username)
                    .or().like(SysUser::getRealName, username);
        }
        if (role != null && !role.isEmpty()) {
            wrapper.eq(SysUser::getRole, role);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }

        wrapper.orderByDesc(SysUser::getCreatedAt);
        return userMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 新增用户
     */
    public void createUser(UserDTO dto) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "123456"));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole() != null ? dto.getRole() : "USER");
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        userMapper.insert(user);
        log.info("新增用户: username={}", user.getUsername());
    }

    /**
     * 修改用户
     */
    public void updateUser(Long id, UserDTO dto) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (dto.getRealName() != null) user.setRealName(dto.getRealName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getRole() != null) user.setRole(dto.getRole());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        userMapper.updateById(user);
        log.info("修改用户: id={}, username={}", id, user.getUsername());
    }

    /**
     * 删除用户
     */
    public void deleteUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("不能删除超级管理员");
        }
        userMapper.deleteById(id);
        log.info("删除用户: id={}, username={}", id, user.getUsername());
    }

    /**
     * 切换用户状态
     */
    public void toggleStatus(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("不能禁用超级管理员");
        }
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        userMapper.updateById(user);
        log.info("切换用户状态: id={}, status={}", id, user.getStatus());
    }

    /**
     * 重置密码
     */
    public void resetPassword(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.updateById(user);
        log.info("重置密码: id={}, username={}", id, user.getUsername());
    }
}

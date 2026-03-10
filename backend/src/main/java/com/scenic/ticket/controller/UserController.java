// -*- coding: utf-8 -*-
package com.scenic.ticket.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scenic.ticket.common.PageResult;
import com.scenic.ticket.common.Result;
import com.scenic.ticket.dto.UserDTO;
import com.scenic.ticket.entity.SysUser;
import com.scenic.ticket.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public Result<PageResult<SysUser>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        IPage<SysUser> result = userService.listUsers(page, size, username, role, status);
        // 隐藏密码 + 脱敏手机号
        result.getRecords().forEach(u -> {
            u.setPassword(null);
            if (u.getPhone() != null && u.getPhone().length() == 11) {
                u.setPhone(u.getPhone().substring(0, 3) + "****" + u.getPhone().substring(7));
            }
            if (u.getEmail() != null && u.getEmail().contains("@")) {
                int atIndex = u.getEmail().indexOf("@");
                if (atIndex > 2) {
                    u.setEmail(u.getEmail().substring(0, 2) + "***" + u.getEmail().substring(atIndex));
                }
            }
        });
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody UserDTO dto) {
        userService.createUser(dto);
        return Result.success("新增用户成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UserDTO dto) {
        userService.updateUser(id, dto);
        return Result.success("修改用户成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除用户成功");
    }

    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        userService.toggleStatus(id);
        return Result.success("操作成功");
    }

    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success("密码已重置为 123456");
    }
}

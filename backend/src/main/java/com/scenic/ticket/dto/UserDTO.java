// -*- coding: utf-8 -*-
package com.scenic.ticket.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户 DTO
 */
@Data
public class UserDTO {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度应在3-20个字符之间")
    private String username;

    @Size(min = 6, max = 32, message = "密码长度应在6-32个字符之间")
    private String password;

    @Size(max = 20, message = "姓名长度不能超过20个字符")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^(ADMIN|USER)$", message = "角色只能是 ADMIN 或 USER")
    private String role;

    private Integer status;
}

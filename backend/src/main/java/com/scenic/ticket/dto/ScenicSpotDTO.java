// -*- coding: utf-8 -*-
package com.scenic.ticket.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 景区 DTO
 */
@Data
public class ScenicSpotDTO {

    private Long id;

    @NotBlank(message = "景区名称不能为空")
    @Size(max = 100, message = "景区名称不能超过100个字符")
    private String name;

    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;

    @Size(max = 200, message = "地址不能超过200个字符")
    private String address;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "开放时间格式应为 HH:mm")
    private String openTime;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "关闭时间格式应为 HH:mm")
    private String closeTime;

    @NotNull(message = "最大容量不能为空")
    @Min(value = 1, message = "最大容量至少为1")
    @Max(value = 500000, message = "最大容量不能超过50万")
    private Integer maxCapacity;

    private String coverImage;

    @Pattern(regexp = "^(\\d{3,4}-)?\\d{7,8}$|^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String contactPhone;

    private Integer status;
}

// -*- coding: utf-8 -*-
package com.scenic.ticket.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建订单请求
 */
@Data
public class OrderCreateDTO {

    @NotNull(message = "景区ID不能为空")
    private Long scenicSpotId;

    @NotBlank(message = "游客姓名不能为空")
    @Size(max = 30, message = "游客姓名不能超过30个字符")
    private String visitorName;

    @NotBlank(message = "游客手机不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String visitorPhone;

    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$",
            message = "身份证号格式不正确")
    private String visitorIdCard;

    @NotNull(message = "游览日期不能为空")
    @FutureOrPresent(message = "游览日期不能早于今天")
    private LocalDate visitDate;

    @Size(max = 200, message = "备注不能超过200个字符")
    private String remark;

    @NotEmpty(message = "请至少选择一个票种")
    @Size(max = 10, message = "单次最多选择10个票种")
    @Valid
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {

        @NotNull(message = "票种ID不能为空")
        private Long ticketTypeId;

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为1")
        @Max(value = 99, message = "单票种数量不能超过99")
        private Integer quantity;
    }
}

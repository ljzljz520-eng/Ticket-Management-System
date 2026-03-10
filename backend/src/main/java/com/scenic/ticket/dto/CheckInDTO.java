// -*- coding: utf-8 -*-
package com.scenic.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 检票请求 DTO
 */
@Data
public class CheckInDTO {

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    private String gateNo;
}

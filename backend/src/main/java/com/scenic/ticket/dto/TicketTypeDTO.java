// -*- coding: utf-8 -*-
package com.scenic.ticket.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 票种 DTO
 */
@Data
public class TicketTypeDTO {

    private Long id;

    @NotNull(message = "景区ID不能为空")
    private Long scenicSpotId;

    @NotBlank(message = "票种名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于0")
    private BigDecimal price;

    private BigDecimal originalPrice;

    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能小于0")
    private Integer stock;

    private Integer dailyLimit;

    @Min(value = 1, message = "有效天数至少为1")
    private Integer validDays;

    private String category;

    private Integer status;
}

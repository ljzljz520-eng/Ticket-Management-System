// -*- coding: utf-8 -*-
package com.scenic.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 票种实体
 */
@Data
@TableName("ticket_type")
public class TicketType {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scenicSpotId;

    private String name;

    private String description;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer stock;

    private Integer dailyLimit;

    private Integer soldToday;

    private Integer validDays;

    private String category;

    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 景区名称（非数据库字段）
     */
    @TableField(exist = false)
    private String scenicSpotName;

    /**
     * 售价（前端字段名兼容）
     */
    @TableField(exist = false)
    private BigDecimal sellPrice;

    /**
     * 今日已售（前端字段名兼容）
     */
    @TableField(exist = false)
    private Integer todaySold;

    public BigDecimal getSellPrice() {
        return this.price;
    }

    public Integer getTodaySold() {
        return this.soldToday;
    }
}

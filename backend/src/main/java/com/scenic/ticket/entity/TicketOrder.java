// -*- coding: utf-8 -*-
package com.scenic.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体
 */
@Data
@TableName("ticket_order")
public class TicketOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long scenicSpotId;

    private BigDecimal totalAmount;

    private BigDecimal actualAmount;

    private String status;

    private String visitorName;

    private String visitorPhone;

    private String visitorIdCard;

    private LocalDate visitDate;

    private String remark;

    private LocalDateTime paidAt;

    private LocalDateTime cancelledAt;

    private LocalDateTime refundedAt;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 订单明细（非数据库字段）
     */
    @TableField(exist = false)
    private List<TicketOrderItem> items;

    /**
     * 景区名称（非数据库字段）
     */
    @TableField(exist = false)
    private String scenicSpotName;

    /**
     * 操作员用户名（非数据库字段）
     */
    @TableField(exist = false)
    private String operatorName;
}

// -*- coding: utf-8 -*-
package com.scenic.ticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细实体
 */
@Data
@TableName("ticket_order_item")
public class TicketOrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long ticketTypeId;

    private String ticketName;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal subtotal;
}

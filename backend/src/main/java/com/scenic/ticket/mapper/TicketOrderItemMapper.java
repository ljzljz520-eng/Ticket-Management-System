// -*- coding: utf-8 -*-
package com.scenic.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scenic.ticket.entity.TicketOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细 Mapper
 */
@Mapper
public interface TicketOrderItemMapper extends BaseMapper<TicketOrderItem> {
}

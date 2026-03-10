// -*- coding: utf-8 -*-
package com.scenic.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scenic.ticket.entity.TicketType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 票种 Mapper
 */
@Mapper
public interface TicketTypeMapper extends BaseMapper<TicketType> {

    /**
     * 扣减库存
     */
    @Update("UPDATE ticket_type SET stock = stock - #{quantity}, sold_today = sold_today + #{quantity} " +
            "WHERE id = #{id} AND stock >= #{quantity}")
    int deductStock(@Param("id") Long id, @Param("quantity") int quantity);

    /**
     * 恢复库存
     */
    @Update("UPDATE ticket_type SET stock = stock + #{quantity}, sold_today = GREATEST(sold_today - #{quantity}, 0) " +
            "WHERE id = #{id}")
    int restoreStock(@Param("id") Long id, @Param("quantity") int quantity);
}

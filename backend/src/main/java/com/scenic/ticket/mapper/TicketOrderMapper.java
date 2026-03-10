// -*- coding: utf-8 -*-
package com.scenic.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenic.ticket.entity.TicketOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 订单 Mapper
 */
@Mapper
public interface TicketOrderMapper extends BaseMapper<TicketOrder> {

    /**
     * 分页查询订单（带关联信息）
     */
    IPage<TicketOrder> selectOrderPage(Page<TicketOrder> page,
                                        @Param("status") String status,
                                        @Param("orderNo") String orderNo,
                                        @Param("visitorName") String visitorName,
                                        @Param("scenicSpotId") Long scenicSpotId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    /**
     * 查询订单详情（带关联信息）
     */
    TicketOrder selectOrderDetail(@Param("id") Long id);

    /**
     * 根据订单号查询订单详情
     */
    TicketOrder selectOrderByNo(@Param("orderNo") String orderNo);

    /**
     * 统计日期范围内的销售额
     */
    BigDecimal sumAmountByDateRange(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate,
                                     @Param("status") String status);

    /**
     * 按日统计销售数据
     */
    List<Map<String, Object>> statisticsByDate(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    /**
     * 按票种统计销售占比
     */
    List<Map<String, Object>> statisticsByTicketType(@Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    /**
     * 按景区统计销售数据
     */
    List<Map<String, Object>> statisticsByScenicSpot(@Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);
}

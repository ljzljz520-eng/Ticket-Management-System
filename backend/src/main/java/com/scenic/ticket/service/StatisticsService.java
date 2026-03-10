// -*- coding: utf-8 -*-
package com.scenic.ticket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scenic.ticket.entity.CheckInRecord;
import com.scenic.ticket.entity.ScenicSpot;
import com.scenic.ticket.entity.TicketOrder;
import com.scenic.ticket.mapper.CheckInRecordMapper;
import com.scenic.ticket.mapper.ScenicSpotMapper;
import com.scenic.ticket.mapper.TicketOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据统计服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final TicketOrderMapper orderMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final ScenicSpotMapper scenicSpotMapper;

    /**
     * 总览数据（不缓存，保证今日游客、当前在园等实时准确）
     */
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        // 今日收入
        BigDecimal todayIncome = orderMapper.sumAmountByDateRange(today, today, "PAID");
        if (todayIncome == null) todayIncome = BigDecimal.ZERO;
        BigDecimal usedIncome = orderMapper.sumAmountByDateRange(today, today, "USED");
        if (usedIncome == null) usedIncome = BigDecimal.ZERO;
        overview.put("todayIncome", todayIncome.add(usedIncome));

        // 本月收入
        BigDecimal monthIncome = orderMapper.sumAmountByDateRange(monthStart, today, "PAID");
        if (monthIncome == null) monthIncome = BigDecimal.ZERO;
        BigDecimal monthUsedIncome = orderMapper.sumAmountByDateRange(monthStart, today, "USED");
        if (monthUsedIncome == null) monthUsedIncome = BigDecimal.ZERO;
        overview.put("monthIncome", monthIncome.add(monthUsedIncome));

        // 今日订单数
        Long todayOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<TicketOrder>()
                        .ge(TicketOrder::getCreatedAt, today.atStartOfDay())
                        .lt(TicketOrder::getCreatedAt, today.plusDays(1).atStartOfDay())
        );
        overview.put("todayOrders", todayOrders);

        // 总订单数
        Long totalOrders = orderMapper.selectCount(null);
        overview.put("totalOrders", totalOrders);

        // 今日游客（今日检票入园人数，与检票管理一致）
        Long todayVisitors = checkInRecordMapper.selectCount(
                new LambdaQueryWrapper<CheckInRecord>()
                        .ge(CheckInRecord::getCheckInTime, today.atStartOfDay())
                        .lt(CheckInRecord::getCheckInTime, today.plusDays(1).atStartOfDay())
        );
        overview.put("todayVisitors", todayVisitors);

        // 当前在园人数（从检票记录实时统计，与检票管理一致，避免 scenic_spot 表不同步）
        Long currentVisitors = checkInRecordMapper.selectCount(
                new LambdaQueryWrapper<CheckInRecord>().eq(CheckInRecord::getStatus, "CHECKED_IN")
        );
        overview.put("currentVisitors", currentVisitors != null ? currentVisitors.intValue() : 0);

        // 景区总数
        List<ScenicSpot> spots = scenicSpotMapper.selectList(null);
        overview.put("scenicSpotCount", spots.size());

        return overview;
    }

    /**
     * 销售趋势（按日）
     */
    public List<Map<String, Object>> getSalesTrend(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        return orderMapper.statisticsByDate(startDate, endDate);
    }

    /**
     * 票种销售占比
     */
    public List<Map<String, Object>> getTicketTypeSales(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        return orderMapper.statisticsByTicketType(startDate, endDate);
    }

    /**
     * 游客趋势
     */
    public List<Map<String, Object>> getVisitorTrend(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        return checkInRecordMapper.visitorStatisticsByDate(startDate, endDate);
    }

    /**
     * 各景区数据对比
     */
    public List<Map<String, Object>> getScenicSpotComparison(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        return orderMapper.statisticsByScenicSpot(startDate, endDate);
    }
}

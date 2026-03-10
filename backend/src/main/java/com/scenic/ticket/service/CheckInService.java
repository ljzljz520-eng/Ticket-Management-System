// -*- coding: utf-8 -*-
package com.scenic.ticket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenic.ticket.common.BusinessException;
import com.scenic.ticket.dto.CheckInDTO;
import com.scenic.ticket.entity.CheckInRecord;
import com.scenic.ticket.entity.ScenicSpot;
import com.scenic.ticket.entity.TicketOrder;
import com.scenic.ticket.mapper.CheckInRecordMapper;
import com.scenic.ticket.mapper.ScenicSpotMapper;
import com.scenic.ticket.mapper.TicketOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 检票服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInService {

    private final CheckInRecordMapper checkInRecordMapper;
    private final TicketOrderMapper orderMapper;
    private final ScenicSpotMapper scenicSpotMapper;
    private final OperationLogService logService;

    /**
     * 检票入园
     */
    @Transactional(rollbackFor = Exception.class)
    public CheckInRecord checkIn(CheckInDTO dto, Long operatorId) {
        // 查询订单
        TicketOrder order = orderMapper.selectOrderByNo(dto.getOrderNo());
        if (order == null) {
            throw new BusinessException("订单不存在: " + dto.getOrderNo());
        }

        // 验证订单状态
        if (!"PAID".equals(order.getStatus())) {
            throw new BusinessException("订单未支付或已使用，当前状态: " + order.getStatus());
        }

        // 验证游览日期
        if (!order.getVisitDate().equals(LocalDate.now())) {
            throw new BusinessException("该票仅限 " + order.getVisitDate() + " 使用");
        }

        // 检查是否已检票
        Long existCount = checkInRecordMapper.selectCount(
                new LambdaQueryWrapper<CheckInRecord>()
                        .eq(CheckInRecord::getOrderNo, dto.getOrderNo())
                        .eq(CheckInRecord::getStatus, "CHECKED_IN")
        );
        if (existCount > 0) {
            throw new BusinessException("该订单已检票入园");
        }

        // 检查景区容量
        ScenicSpot spot = scenicSpotMapper.selectById(order.getScenicSpotId());
        if (spot != null && spot.getCurrentVisitors() >= spot.getMaxCapacity()) {
            throw new BusinessException("景区已达最大容量，暂停入园");
        }

        // 创建检票记录
        CheckInRecord record = new CheckInRecord();
        record.setOrderId(order.getId());
        record.setOrderNo(order.getOrderNo());
        record.setScenicSpotId(order.getScenicSpotId());
        record.setVisitorName(order.getVisitorName());
        record.setCheckInTime(LocalDateTime.now());
        record.setStatus("CHECKED_IN");
        record.setOperatorId(operatorId);
        record.setGateNo(dto.getGateNo());

        checkInRecordMapper.insert(record);

        // 更新订单状态为已使用
        order.setStatus("USED");
        orderMapper.updateById(order);

        // 更新景区在园人数
        if (spot != null) {
            spot.setCurrentVisitors(spot.getCurrentVisitors() + 1);
            scenicSpotMapper.updateById(spot);
        }

        log.info("检票入园: orderNo={}, visitor={}", order.getOrderNo(), order.getVisitorName());
        return record;
    }

    /**
     * 检票出园
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkOut(Long recordId) {
        CheckInRecord record = checkInRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("检票记录不存在");
        }
        if (!"CHECKED_IN".equals(record.getStatus())) {
            throw new BusinessException("该游客已出园");
        }

        record.setStatus("CHECKED_OUT");
        record.setCheckOutTime(LocalDateTime.now());
        checkInRecordMapper.updateById(record);

        // 减少在园人数
        ScenicSpot spot = scenicSpotMapper.selectById(record.getScenicSpotId());
        if (spot != null && spot.getCurrentVisitors() > 0) {
            spot.setCurrentVisitors(spot.getCurrentVisitors() - 1);
            scenicSpotMapper.updateById(spot);
        }

        log.info("检票出园: recordId={}, visitor={}", recordId, record.getVisitorName());
    }

    /**
     * 分页查询检票记录
     */
    public IPage<CheckInRecord> listRecords(int page, int size, Long scenicSpotId,
                                             String status, LocalDate date) {
        return checkInRecordMapper.selectRecordPage(new Page<>(page, size), scenicSpotId, status, date);
    }

    /**
     * 今日检票统计（统一返回 camelCase 供前端使用）
     */
    public Map<String, Object> todayStatistics() {
        Map<String, Object> raw = checkInRecordMapper.todayStatistics(LocalDate.now());
        Map<String, Object> result = new HashMap<>();
        result.put("totalCheckIn", getLongOrDefault(raw, "totalCheckIn", "total_check_in", 0L));
        result.put("inPark", getLongOrDefault(raw, "inPark", "currently_in", 0L));
        result.put("checkedOut", getLongOrDefault(raw, "checkedOut", "checked_out", 0L));
        return result;
    }

    private long getLongOrDefault(Map<String, Object> map, String key1, String key2, long def) {
        Object v = map != null ? (map.get(key1) != null ? map.get(key1) : map.get(key2)) : null;
        if (v == null) return def;
        if (v instanceof Number) return ((Number) v).longValue();
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return def;
        }
    }
}

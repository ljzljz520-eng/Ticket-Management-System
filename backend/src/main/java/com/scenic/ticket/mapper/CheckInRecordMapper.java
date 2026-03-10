// -*- coding: utf-8 -*-
package com.scenic.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenic.ticket.entity.CheckInRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 检票记录 Mapper
 */
@Mapper
public interface CheckInRecordMapper extends BaseMapper<CheckInRecord> {

    /**
     * 分页查询检票记录（带关联信息）
     */
    IPage<CheckInRecord> selectRecordPage(Page<CheckInRecord> page,
                                           @Param("scenicSpotId") Long scenicSpotId,
                                           @Param("status") String status,
                                           @Param("date") LocalDate date);

    /**
     * 今日检票统计
     */
    Map<String, Object> todayStatistics(@Param("date") LocalDate date);

    /**
     * 按日统计游客数
     */
    List<Map<String, Object>> visitorStatisticsByDate(@Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);
}

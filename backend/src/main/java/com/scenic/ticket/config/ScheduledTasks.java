// -*- coding: utf-8 -*-
package com.scenic.ticket.config;

import com.scenic.ticket.mapper.TicketTypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务配置
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final TicketTypeMapper ticketTypeMapper;

    /**
     * 每日凌晨0点重置所有票种的今日已售数量
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetDailySold() {
        int count = ticketTypeMapper.resetSoldToday();
        log.info("每日已售数量重置完成，共重置 {} 个票种", count);
    }
}
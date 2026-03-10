// -*- coding: utf-8 -*-
package com.scenic.ticket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenic.ticket.entity.OperationLog;
import com.scenic.ticket.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 操作日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper logMapper;

    /**
     * 异步保存操作日志（简化版）
     */
    @Async
    public void saveLog(Long userId, String username, String operation, String method, String params) {
        saveLogWithDetails(userId, username, operation, method, params, null, 1, null, null);
    }

    /**
     * 异步保存操作日志（完整版，含 IP、状态、耗时）
     */
    @Async
    public void saveLogWithDetails(Long userId, String username, String operation,
                                    String method, String params, String ip,
                                    Integer status, String errorMsg, Long duration) {
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(userId);
            operationLog.setUsername(username);
            operationLog.setOperation(operation);
            operationLog.setMethod(method);
            operationLog.setParams(params);
            operationLog.setIp(ip);
            operationLog.setStatus(status != null ? status : 1);
            operationLog.setErrorMsg(errorMsg);
            operationLog.setDuration(duration);
            operationLog.setCreatedAt(LocalDateTime.now());
            logMapper.insert(operationLog);
        } catch (Exception e) {
            log.error("保存操作日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 分页查询日志
     */
    public IPage<OperationLog> listLogs(int page, int size, String username,
                                         String operation, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        if (username != null && !username.isEmpty()) {
            wrapper.like(OperationLog::getUsername, username);
        }
        if (operation != null && !operation.isEmpty()) {
            wrapper.like(OperationLog::getOperation, operation);
        }
        if (startDate != null) {
            wrapper.ge(OperationLog::getCreatedAt, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(OperationLog::getCreatedAt, endDate.plusDays(1).atStartOfDay());
        }

        wrapper.orderByDesc(OperationLog::getCreatedAt);
        return logMapper.selectPage(new Page<>(page, size), wrapper);
    }
}

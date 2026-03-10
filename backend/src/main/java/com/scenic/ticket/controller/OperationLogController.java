// -*- coding: utf-8 -*-
package com.scenic.ticket.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scenic.ticket.common.PageResult;
import com.scenic.ticket.common.Result;
import com.scenic.ticket.entity.OperationLog;
import com.scenic.ticket.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OperationLogController {

    private final OperationLogService logService;

    @GetMapping
    public Result<PageResult<OperationLog>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        IPage<OperationLog> result = logService.listLogs(page, size, username, operation, startDate, endDate);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }
}

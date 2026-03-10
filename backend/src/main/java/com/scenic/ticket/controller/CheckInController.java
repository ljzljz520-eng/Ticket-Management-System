// -*- coding: utf-8 -*-
package com.scenic.ticket.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scenic.ticket.common.PageResult;
import com.scenic.ticket.common.Result;
import com.scenic.ticket.dto.CheckInDTO;
import com.scenic.ticket.entity.CheckInRecord;
import com.scenic.ticket.service.CheckInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * 检票管理控制器
 */
@RestController
@RequestMapping("/api/check-in")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    public Result<CheckInRecord> checkIn(@Valid @RequestBody CheckInDTO dto, Authentication authentication) {
        Long operatorId = (Long) authentication.getCredentials();
        CheckInRecord record = checkInService.checkIn(dto, operatorId);
        return Result.success("检票入园成功", record);
    }

    @PutMapping("/{id}/check-out")
    public Result<Void> checkOut(@PathVariable Long id) {
        checkInService.checkOut(id);
        return Result.success("出园登记成功");
    }

    @GetMapping("/records")
    public Result<PageResult<CheckInRecord>> listRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long scenicSpotId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        IPage<CheckInRecord> result = checkInService.listRecords(page, size, scenicSpotId, status, date);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @GetMapping("/today")
    public Result<Map<String, Object>> todayStatistics() {
        return Result.success(checkInService.todayStatistics());
    }
}

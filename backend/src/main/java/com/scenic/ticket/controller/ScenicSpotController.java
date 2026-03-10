// -*- coding: utf-8 -*-
package com.scenic.ticket.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scenic.ticket.common.PageResult;
import com.scenic.ticket.common.Result;
import com.scenic.ticket.dto.ScenicSpotDTO;
import com.scenic.ticket.entity.ScenicSpot;
import com.scenic.ticket.service.ScenicSpotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 景区管理控制器
 */
@RestController
@RequestMapping("/api/scenic-spots")
@RequiredArgsConstructor
public class ScenicSpotController {

    private final ScenicSpotService spotService;

    @GetMapping
    public Result<PageResult<ScenicSpot>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        IPage<ScenicSpot> result = spotService.listSpots(page, size, name, status);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @GetMapping("/active")
    public Result<List<ScenicSpot>> listActive() {
        return Result.success(spotService.listActiveSpots());
    }

    @GetMapping("/{id}")
    public Result<ScenicSpot> detail(@PathVariable Long id) {
        return Result.success(spotService.getSpotById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> create(@Valid @RequestBody ScenicSpotDTO dto) {
        spotService.createSpot(dto);
        return Result.success("新增景区成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ScenicSpotDTO dto) {
        spotService.updateSpot(id, dto);
        return Result.success("修改景区成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        spotService.deleteSpot(id);
        return Result.success("删除景区成功");
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        spotService.toggleStatus(id);
        return Result.success("操作成功");
    }
}

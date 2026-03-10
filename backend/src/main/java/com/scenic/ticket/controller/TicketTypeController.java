// -*- coding: utf-8 -*-
package com.scenic.ticket.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scenic.ticket.common.PageResult;
import com.scenic.ticket.common.Result;
import com.scenic.ticket.dto.TicketTypeDTO;
import com.scenic.ticket.entity.TicketType;
import com.scenic.ticket.service.TicketTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 票种管理控制器
 */
@RestController
@RequestMapping("/api/ticket-types")
@RequiredArgsConstructor
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @GetMapping
    public Result<PageResult<TicketType>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long scenicSpotId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String name) {
        IPage<TicketType> result = ticketTypeService.listTicketTypes(page, size, scenicSpotId, category, status, name);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @GetMapping("/active")
    public Result<List<TicketType>> listActive(@RequestParam Long scenicSpotId) {
        return Result.success(ticketTypeService.listActiveBySpot(scenicSpotId));
    }

    @GetMapping("/{id}")
    public Result<TicketType> detail(@PathVariable Long id) {
        return Result.success(ticketTypeService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> create(@Valid @RequestBody TicketTypeDTO dto) {
        ticketTypeService.create(dto);
        return Result.success("新增票种成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TicketTypeDTO dto) {
        ticketTypeService.update(id, dto);
        return Result.success("修改票种成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        ticketTypeService.delete(id);
        return Result.success("删除票种成功");
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        ticketTypeService.toggleStatus(id);
        return Result.success("操作成功");
    }

    @PutMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> adjustStock(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer stock = body.get("stock");
        if (stock == null) {
            return Result.error(400, "库存不能为空");
        }
        ticketTypeService.adjustStock(id, stock);
        return Result.success("库存调整成功");
    }
}

// -*- coding: utf-8 -*-
package com.scenic.ticket.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scenic.ticket.common.PageResult;
import com.scenic.ticket.common.Result;
import com.scenic.ticket.dto.OrderCreateDTO;
import com.scenic.ticket.entity.TicketOrder;
import com.scenic.ticket.service.TicketOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 订单管理控制器
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class TicketOrderController {

    private final TicketOrderService orderService;

    @GetMapping
    public Result<PageResult<TicketOrder>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String visitorName,
            @RequestParam(required = false) Long scenicSpotId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        IPage<TicketOrder> result = orderService.listOrders(page, size, status, orderNo,
                visitorName, scenicSpotId, startDate, endDate);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @GetMapping("/{id}")
    public Result<TicketOrder> detail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }

    @GetMapping("/check/{orderNo}")
    public Result<TicketOrder> checkOrder(@PathVariable String orderNo) {
        return Result.success(orderService.getOrderByNo(orderNo));
    }

    @PostMapping
    public Result<TicketOrder> create(@Valid @RequestBody OrderCreateDTO dto, Authentication authentication) {
        Long userId = (Long) authentication.getCredentials();
        TicketOrder order = orderService.createOrder(dto, userId);
        return Result.success("创建订单成功", order);
    }

    @PutMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id) {
        orderService.payOrder(id);
        return Result.success("支付成功");
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return Result.success("取消成功");
    }

    @PutMapping("/{id}/refund")
    public Result<Void> refund(@PathVariable Long id) {
        orderService.refundOrder(id);
        return Result.success("退款成功");
    }
}

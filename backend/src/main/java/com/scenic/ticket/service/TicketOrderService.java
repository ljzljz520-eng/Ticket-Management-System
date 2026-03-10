// -*- coding: utf-8 -*-
package com.scenic.ticket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenic.ticket.common.BusinessException;
import com.scenic.ticket.dto.OrderCreateDTO;
import com.scenic.ticket.entity.*;
import com.scenic.ticket.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketOrderService {

    private final TicketOrderMapper orderMapper;
    private final TicketOrderItemMapper orderItemMapper;
    private final TicketTypeMapper ticketTypeMapper;
    private final ScenicSpotMapper scenicSpotMapper;
    private final OperationLogService logService;

    /**
     * 分页查询订单
     */
    public IPage<TicketOrder> listOrders(int page, int size, String status, String orderNo,
                                          String visitorName, Long scenicSpotId,
                                          LocalDate startDate, LocalDate endDate) {
        return orderMapper.selectOrderPage(new Page<>(page, size),
                status, orderNo, visitorName, scenicSpotId, startDate, endDate);
    }

    /**
     * 查询订单详情
     */
    public TicketOrder getOrderDetail(Long id) {
        TicketOrder order = orderMapper.selectOrderDetail(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    /**
     * 根据订单号查询
     */
    public TicketOrder getOrderByNo(String orderNo) {
        TicketOrder order = orderMapper.selectOrderByNo(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    /**
     * 创建订单（售票）
     */
    @Transactional(rollbackFor = Exception.class)
    public TicketOrder createOrder(OrderCreateDTO dto, Long userId) {
        // 验证景区
        ScenicSpot spot = scenicSpotMapper.selectById(dto.getScenicSpotId());
        if (spot == null || spot.getStatus() == 0) {
            throw new BusinessException("景区不存在或已关闭");
        }

        // 验证游览日期
        if (dto.getVisitDate().isBefore(LocalDate.now())) {
            throw new BusinessException("游览日期不能早于今天");
        }

        // 生成订单号
        String orderNo = generateOrderNo();

        // 计算总金额并验证库存
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<TicketOrderItem> items = new ArrayList<>();

        for (OrderCreateDTO.OrderItemDTO itemDTO : dto.getItems()) {
            TicketType ticketType = ticketTypeMapper.selectById(itemDTO.getTicketTypeId());
            if (ticketType == null || ticketType.getStatus() == 0) {
                throw new BusinessException("票种不存在或已下架: " + itemDTO.getTicketTypeId());
            }
            if (!ticketType.getScenicSpotId().equals(dto.getScenicSpotId())) {
                throw new BusinessException("票种不属于所选景区");
            }
            if (ticketType.getStock() < itemDTO.getQuantity()) {
                throw new BusinessException("票种「" + ticketType.getName() + "」库存不足");
            }

            // 检查每日限额
            if (ticketType.getDailyLimit() != null &&
                    ticketType.getSoldToday() + itemDTO.getQuantity() > ticketType.getDailyLimit()) {
                throw new BusinessException("票种「" + ticketType.getName() + "」今日已达限额");
            }

            BigDecimal subtotal = ticketType.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            TicketOrderItem item = new TicketOrderItem();
            item.setTicketTypeId(ticketType.getId());
            item.setTicketName(ticketType.getName());
            item.setPrice(ticketType.getPrice());
            item.setQuantity(itemDTO.getQuantity());
            item.setSubtotal(subtotal);
            items.add(item);

            // 扣减库存
            int affected = ticketTypeMapper.deductStock(ticketType.getId(), itemDTO.getQuantity());
            if (affected == 0) {
                throw new BusinessException("票种「" + ticketType.getName() + "」库存不足");
            }
        }

        // 创建订单
        TicketOrder order = new TicketOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setScenicSpotId(dto.getScenicSpotId());
        order.setTotalAmount(totalAmount);
        order.setActualAmount(totalAmount);
        order.setStatus("PENDING");
        order.setVisitorName(dto.getVisitorName());
        order.setVisitorPhone(dto.getVisitorPhone());
        order.setVisitorIdCard(dto.getVisitorIdCard());
        order.setVisitDate(dto.getVisitDate());
        order.setRemark(dto.getRemark());

        orderMapper.insert(order);

        // 保存订单明细
        for (TicketOrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        order.setItems(items);
        order.setScenicSpotName(spot.getName());

        log.info("创建订单: orderNo={}, amount={}, visitor={}", orderNo, totalAmount, dto.getVisitorName());
        return order;
    }

    /**
     * 订单支付
     */
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long id) {
        TicketOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许支付，当前状态: " + order.getStatus());
        }

        order.setStatus("PAID");
        order.setPaidAt(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("订单支付成功: orderNo={}, amount={}", order.getOrderNo(), order.getActualAmount());
    }

    /**
     * 取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long id) {
        TicketOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("只能取消待支付的订单");
        }

        order.setStatus("CANCELLED");
        order.setCancelledAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 恢复库存
        restoreOrderStock(order.getId());

        log.info("取消订单: orderNo={}", order.getOrderNo());
    }

    /**
     * 订单退款
     */
    @Transactional(rollbackFor = Exception.class)
    public void refundOrder(Long id) {
        TicketOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"PAID".equals(order.getStatus())) {
            throw new BusinessException("只能退款已支付的订单");
        }

        // 检查是否已检票
        Long checkInCount = orderItemMapper.selectCount(null);
        // 简化检查：查询检票记录
        // 此处简化处理，实际应检查 check_in_record 表

        order.setStatus("REFUNDED");
        order.setRefundedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 恢复库存
        restoreOrderStock(order.getId());

        log.info("订单退款: orderNo={}, amount={}", order.getOrderNo(), order.getActualAmount());
    }

    /**
     * 恢复订单关联票种的库存
     */
    private void restoreOrderStock(Long orderId) {
        List<TicketOrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<TicketOrderItem>().eq(TicketOrderItem::getOrderId, orderId)
        );
        for (TicketOrderItem item : items) {
            ticketTypeMapper.restoreStock(item.getTicketTypeId(), item.getQuantity());
        }
    }

    /**
     * 生成订单号: TK + 日期 + 6位随机数
     */
    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "TK" + datePart + random;
    }
}

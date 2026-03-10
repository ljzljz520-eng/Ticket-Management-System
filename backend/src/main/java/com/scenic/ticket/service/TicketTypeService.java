// -*- coding: utf-8 -*-
package com.scenic.ticket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenic.ticket.common.BusinessException;
import com.scenic.ticket.dto.TicketTypeDTO;
import com.scenic.ticket.entity.ScenicSpot;
import com.scenic.ticket.entity.TicketType;
import com.scenic.ticket.mapper.ScenicSpotMapper;
import com.scenic.ticket.mapper.TicketTypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 票种管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketTypeService {

    private final TicketTypeMapper ticketTypeMapper;
    private final ScenicSpotMapper scenicSpotMapper;
    private final OperationLogService logService;

    /**
     * 分页查询票种
     */
    public IPage<TicketType> listTicketTypes(int page, int size, Long scenicSpotId,
                                              String category, Integer status, String name) {
        LambdaQueryWrapper<TicketType> wrapper = new LambdaQueryWrapper<>();

        if (scenicSpotId != null) {
            wrapper.eq(TicketType::getScenicSpotId, scenicSpotId);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(TicketType::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(TicketType::getStatus, status);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like(TicketType::getName, name);
        }

        wrapper.orderByDesc(TicketType::getCreatedAt);
        IPage<TicketType> result = ticketTypeMapper.selectPage(new Page<>(page, size), wrapper);

        // 填充景区名称
        result.getRecords().forEach(tt -> {
            ScenicSpot spot = scenicSpotMapper.selectById(tt.getScenicSpotId());
            if (spot != null) {
                tt.setScenicSpotName(spot.getName());
            }
        });

        return result;
    }

    /**
     * 获取某景区下启用的票种（售票用）
     */
    public List<TicketType> listActiveBySpot(Long scenicSpotId) {
        return ticketTypeMapper.selectList(
                new LambdaQueryWrapper<TicketType>()
                        .eq(TicketType::getScenicSpotId, scenicSpotId)
                        .eq(TicketType::getStatus, 1)
                        .gt(TicketType::getStock, 0)
                        .orderByAsc(TicketType::getPrice)
        );
    }

    /**
     * 获取票种详情
     */
    public TicketType getById(Long id) {
        TicketType tt = ticketTypeMapper.selectById(id);
        if (tt == null) {
            throw new BusinessException("票种不存在");
        }
        ScenicSpot spot = scenicSpotMapper.selectById(tt.getScenicSpotId());
        if (spot != null) {
            tt.setScenicSpotName(spot.getName());
        }
        return tt;
    }

    /**
     * 新增票种
     */
    public void create(TicketTypeDTO dto) {
        ScenicSpot spot = scenicSpotMapper.selectById(dto.getScenicSpotId());
        if (spot == null) {
            throw new BusinessException("关联景区不存在");
        }

        TicketType tt = new TicketType();
        tt.setScenicSpotId(dto.getScenicSpotId());
        tt.setName(dto.getName());
        tt.setDescription(dto.getDescription());
        tt.setPrice(dto.getPrice());
        tt.setOriginalPrice(dto.getOriginalPrice());
        tt.setStock(dto.getStock());
        tt.setDailyLimit(dto.getDailyLimit());
        tt.setSoldToday(0);
        tt.setValidDays(dto.getValidDays() != null ? dto.getValidDays() : 1);
        tt.setCategory(dto.getCategory() != null ? dto.getCategory() : "ADULT");
        tt.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        ticketTypeMapper.insert(tt);
        log.info("新增票种: name={}, scenicSpot={}", tt.getName(), spot.getName());
    }

    /**
     * 修改票种
     */
    public void update(Long id, TicketTypeDTO dto) {
        TicketType tt = ticketTypeMapper.selectById(id);
        if (tt == null) {
            throw new BusinessException("票种不存在");
        }

        if (dto.getName() != null) tt.setName(dto.getName());
        if (dto.getDescription() != null) tt.setDescription(dto.getDescription());
        if (dto.getPrice() != null) tt.setPrice(dto.getPrice());
        if (dto.getOriginalPrice() != null) tt.setOriginalPrice(dto.getOriginalPrice());
        if (dto.getStock() != null) tt.setStock(dto.getStock());
        if (dto.getDailyLimit() != null) tt.setDailyLimit(dto.getDailyLimit());
        if (dto.getValidDays() != null) tt.setValidDays(dto.getValidDays());
        if (dto.getCategory() != null) tt.setCategory(dto.getCategory());

        ticketTypeMapper.updateById(tt);
        log.info("修改票种: id={}, name={}", id, tt.getName());
    }

    /**
     * 删除票种
     */
    public void delete(Long id) {
        TicketType tt = ticketTypeMapper.selectById(id);
        if (tt == null) {
            throw new BusinessException("票种不存在");
        }
        ticketTypeMapper.deleteById(id);
        log.info("删除票种: id={}, name={}", id, tt.getName());
    }

    /**
     * 上架/下架票种
     */
    public void toggleStatus(Long id) {
        TicketType tt = ticketTypeMapper.selectById(id);
        if (tt == null) {
            throw new BusinessException("票种不存在");
        }
        tt.setStatus(tt.getStatus() == 1 ? 0 : 1);
        ticketTypeMapper.updateById(tt);
        log.info("切换票种状态: id={}, status={}", id, tt.getStatus());
    }

    /**
     * 调整库存
     */
    public void adjustStock(Long id, Integer stock) {
        TicketType tt = ticketTypeMapper.selectById(id);
        if (tt == null) {
            throw new BusinessException("票种不存在");
        }
        if (stock < 0) {
            throw new BusinessException("库存不能小于0");
        }
        tt.setStock(stock);
        ticketTypeMapper.updateById(tt);
        log.info("调整库存: id={}, name={}, stock={}", id, tt.getName(), stock);
    }
}

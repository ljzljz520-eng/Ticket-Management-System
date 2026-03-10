// -*- coding: utf-8 -*-
package com.scenic.ticket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenic.ticket.common.BusinessException;
import com.scenic.ticket.dto.ScenicSpotDTO;
import com.scenic.ticket.entity.ScenicSpot;
import com.scenic.ticket.mapper.ScenicSpotMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 景区管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScenicSpotService {

    private final ScenicSpotMapper spotMapper;
    private final OperationLogService logService;

    /**
     * 分页查询景区
     */
    public IPage<ScenicSpot> listSpots(int page, int size, String name, Integer status) {
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();

        if (name != null && !name.isEmpty()) {
            wrapper.like(ScenicSpot::getName, name);
        }
        if (status != null) {
            wrapper.eq(ScenicSpot::getStatus, status);
        }

        wrapper.orderByDesc(ScenicSpot::getCreatedAt);
        return spotMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 获取所有启用的景区列表（用于下拉选择）
     * 注：不使用缓存避免 Redis 序列化 LocalDateTime 等导致的 500
     */
    public List<ScenicSpot> listActiveSpots() {
        return spotMapper.selectList(
                new LambdaQueryWrapper<ScenicSpot>()
                        .eq(ScenicSpot::getStatus, 1)
                        .orderByAsc(ScenicSpot::getName)
        );
    }

    /**
     * 获取景区详情
     */
    public ScenicSpot getSpotById(Long id) {
        ScenicSpot spot = spotMapper.selectById(id);
        if (spot == null) {
            throw new BusinessException("景区不存在");
        }
        return spot;
    }

    /**
     * 新增景区
     */
    @CacheEvict(value = "scenicSpots", allEntries = true)
    public void createSpot(ScenicSpotDTO dto) {
        ScenicSpot spot = new ScenicSpot();
        spot.setName(dto.getName());
        spot.setDescription(dto.getDescription());
        spot.setAddress(dto.getAddress());
        spot.setOpenTime(dto.getOpenTime() != null ? dto.getOpenTime() : "08:00");
        spot.setCloseTime(dto.getCloseTime() != null ? dto.getCloseTime() : "18:00");
        spot.setMaxCapacity(dto.getMaxCapacity());
        spot.setCoverImage(dto.getCoverImage());
        spot.setContactPhone(dto.getContactPhone());
        spot.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        spot.setCurrentVisitors(0);

        spotMapper.insert(spot);
        log.info("新增景区: name={}", spot.getName());
    }

    /**
     * 修改景区
     */
    @CacheEvict(value = "scenicSpots", allEntries = true)
    public void updateSpot(Long id, ScenicSpotDTO dto) {
        ScenicSpot spot = spotMapper.selectById(id);
        if (spot == null) {
            throw new BusinessException("景区不存在");
        }

        if (dto.getName() != null) spot.setName(dto.getName());
        if (dto.getDescription() != null) spot.setDescription(dto.getDescription());
        if (dto.getAddress() != null) spot.setAddress(dto.getAddress());
        if (dto.getOpenTime() != null) spot.setOpenTime(dto.getOpenTime());
        if (dto.getCloseTime() != null) spot.setCloseTime(dto.getCloseTime());
        if (dto.getMaxCapacity() != null) spot.setMaxCapacity(dto.getMaxCapacity());
        if (dto.getCoverImage() != null) spot.setCoverImage(dto.getCoverImage());
        if (dto.getContactPhone() != null) spot.setContactPhone(dto.getContactPhone());

        spotMapper.updateById(spot);
        log.info("修改景区: id={}, name={}", id, spot.getName());
    }

    /**
     * 删除景区
     */
    @CacheEvict(value = "scenicSpots", allEntries = true)
    public void deleteSpot(Long id) {
        ScenicSpot spot = spotMapper.selectById(id);
        if (spot == null) {
            throw new BusinessException("景区不存在");
        }
        spotMapper.deleteById(id);
        log.info("删除景区: id={}, name={}", id, spot.getName());
    }

    /**
     * 切换景区状态
     */
    @CacheEvict(value = "scenicSpots", allEntries = true)
    public void toggleStatus(Long id) {
        ScenicSpot spot = spotMapper.selectById(id);
        if (spot == null) {
            throw new BusinessException("景区不存在");
        }
        spot.setStatus(spot.getStatus() == 1 ? 0 : 1);
        spotMapper.updateById(spot);
        log.info("切换景区状态: id={}, status={}", id, spot.getStatus());
    }
}

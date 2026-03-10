// -*- coding: utf-8 -*-
package com.scenic.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 景区实体
 */
@Data
@TableName("scenic_spot")
public class ScenicSpot {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String address;

    private String openTime;

    private String closeTime;

    private Integer maxCapacity;

    private Integer currentVisitors;

    private String coverImage;

    private String contactPhone;

    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

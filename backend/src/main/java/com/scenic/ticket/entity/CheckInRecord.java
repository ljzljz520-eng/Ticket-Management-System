// -*- coding: utf-8 -*-
package com.scenic.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 检票记录实体
 */
@Data
@TableName("check_in_record")
public class CheckInRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String orderNo;

    private Long scenicSpotId;

    private String visitorName;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private String status;

    private Long operatorId;

    private String gateNo;

    private LocalDateTime createdAt;

    /**
     * 景区名称（非数据库字段）
     */
    @TableField(exist = false)
    private String scenicSpotName;

    /**
     * 操作员姓名（非数据库字段）
     */
    @TableField(exist = false)
    private String operatorName;
}

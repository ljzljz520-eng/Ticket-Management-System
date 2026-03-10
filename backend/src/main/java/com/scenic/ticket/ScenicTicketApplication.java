// -*- coding: utf-8 -*-
package com.scenic.ticket;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 景区票务管理系统 - 启动类
 */
@SpringBootApplication
@MapperScan("com.scenic.ticket.mapper")
@EnableCaching
@EnableAsync
public class ScenicTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScenicTicketApplication.class, args);
    }
}

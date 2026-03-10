-- -*- coding: utf-8 -*-
-- 景区票务管理系统 - 数据库初始化脚本
-- 编码：UTF-8 / utf8mb4

CREATE DATABASE IF NOT EXISTS scenic_ticket DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE scenic_ticket;

-- ========================================
-- 系统用户表
-- ========================================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(255) COMMENT '头像URL',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN/USER',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- ========================================
-- 景区表
-- ========================================
CREATE TABLE IF NOT EXISTS scenic_spot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '景区名称',
    description TEXT COMMENT '景区描述',
    address VARCHAR(255) COMMENT '景区地址',
    open_time VARCHAR(50) DEFAULT '08:00' COMMENT '开放时间',
    close_time VARCHAR(50) DEFAULT '18:00' COMMENT '关闭时间',
    max_capacity INT NOT NULL DEFAULT 5000 COMMENT '最大容量',
    current_visitors INT NOT NULL DEFAULT 0 COMMENT '当前在园人数',
    cover_image VARCHAR(255) COMMENT '封面图片URL',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-关闭 1-开放',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='景区表';

-- ========================================
-- 票种表
-- ========================================
CREATE TABLE IF NOT EXISTS ticket_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    scenic_spot_id BIGINT NOT NULL COMMENT '所属景区ID',
    name VARCHAR(100) NOT NULL COMMENT '票种名称',
    description VARCHAR(500) COMMENT '票种描述',
    price DECIMAL(10,2) NOT NULL COMMENT '售价',
    original_price DECIMAL(10,2) COMMENT '原价',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存',
    daily_limit INT DEFAULT NULL COMMENT '每日限额',
    sold_today INT NOT NULL DEFAULT 0 COMMENT '今日已售',
    valid_days INT NOT NULL DEFAULT 1 COMMENT '有效天数',
    category VARCHAR(20) NOT NULL DEFAULT 'ADULT' COMMENT '类别: ADULT/CHILD/SENIOR/STUDENT/GROUP',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_scenic_spot_id (scenic_spot_id),
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='票种表';

-- ========================================
-- 订单表
-- ========================================
CREATE TABLE IF NOT EXISTS ticket_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '操作员ID',
    scenic_spot_id BIGINT NOT NULL COMMENT '景区ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
    actual_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/PAID/CANCELLED/REFUNDED/USED',
    visitor_name VARCHAR(50) COMMENT '游客姓名',
    visitor_phone VARCHAR(20) COMMENT '游客手机',
    visitor_id_card VARCHAR(20) COMMENT '游客身份证',
    visit_date DATE NOT NULL COMMENT '游览日期',
    remark VARCHAR(500) COMMENT '备注',
    paid_at DATETIME COMMENT '支付时间',
    cancelled_at DATETIME COMMENT '取消时间',
    refunded_at DATETIME COMMENT '退款时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_scenic_spot_id (scenic_spot_id),
    INDEX idx_status (status),
    INDEX idx_visit_date (visit_date),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ========================================
-- 订单明细表
-- ========================================
CREATE TABLE IF NOT EXISTS ticket_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    ticket_type_id BIGINT NOT NULL COMMENT '票种ID',
    ticket_name VARCHAR(100) COMMENT '票种名称(冗余)',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计',
    INDEX idx_order_id (order_id),
    INDEX idx_ticket_type_id (ticket_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- ========================================
-- 检票记录表
-- ========================================
CREATE TABLE IF NOT EXISTS check_in_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    scenic_spot_id BIGINT NOT NULL COMMENT '景区ID',
    visitor_name VARCHAR(50) COMMENT '游客姓名',
    check_in_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入园时间',
    check_out_time DATETIME COMMENT '出园时间',
    status VARCHAR(20) NOT NULL DEFAULT 'CHECKED_IN' COMMENT '状态: CHECKED_IN/CHECKED_OUT',
    operator_id BIGINT COMMENT '操作员ID',
    gate_no VARCHAR(20) COMMENT '闸机编号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id),
    INDEX idx_order_no (order_no),
    INDEX idx_scenic_spot_id (scenic_spot_id),
    INDEX idx_check_in_time (check_in_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='检票记录表';

-- ========================================
-- 操作日志表
-- ========================================
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '操作人ID',
    username VARCHAR(50) COMMENT '操作人用户名',
    operation VARCHAR(100) NOT NULL COMMENT '操作描述',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-失败 1-成功',
    error_msg TEXT COMMENT '错误信息',
    duration BIGINT COMMENT '耗时(ms)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ========================================
-- 初始化数据
-- ========================================

-- 管理员账号 (密码: admin123, BCrypt加密)
INSERT INTO sys_user (username, password, real_name, phone, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800000001', 'ADMIN', 1);

-- 普通用户账号 (密码: user123, BCrypt加密)
INSERT INTO sys_user (username, password, real_name, phone, role, status) VALUES
('user', '$2a$10$VcdzH8Q.o4KEo6df.PYoRO1fJwyyMbGnMZ0P.uRlRb1I3MnfWyqGy', '售票员张三', '13800000002', 'USER', 1);

-- 示例景区数据
INSERT INTO scenic_spot (name, description, address, open_time, close_time, max_capacity, contact_phone, status) VALUES
('九寨沟风景区', '九寨沟位于四川省阿坝藏族羌族自治州九寨沟县，以翠海、叠瀑、彩林、雪峰、藏情闻名于世。', '四川省阿坝州九寨沟县', '07:00', '18:00', 20000, '0837-7739753', 1),
('黄山风景区', '黄山位于安徽省黄山市，以奇松、怪石、云海、温泉、冬雪"五绝"著称。', '安徽省黄山市黄山区', '06:30', '17:30', 25000, '0559-5580327', 1),
('张家界国家森林公园', '张家界国家森林公园位于湖南省张家界市，以峰称奇、以谷显幽、以林见秀。', '湖南省张家界市武陵源区', '07:00', '18:00', 15000, '0744-5712189', 1);

-- 示例票种数据
INSERT INTO ticket_type (scenic_spot_id, name, description, price, original_price, stock, daily_limit, category, status) VALUES
(1, '九寨沟成人票', '九寨沟风景区成人全价门票', 169.00, 250.00, 5000, 2000, 'ADULT', 1),
(1, '九寨沟儿童票', '1.2m-1.5m儿童优惠票', 80.00, 169.00, 2000, 1000, 'CHILD', 1),
(1, '九寨沟老人票', '60岁以上老人优惠票', 80.00, 169.00, 2000, 1000, 'SENIOR', 1),
(1, '九寨沟学生票', '全日制在校学生优惠票', 80.00, 169.00, 3000, 1500, 'STUDENT', 1),
(2, '黄山成人票', '黄山风景区成人全价门票', 190.00, 230.00, 8000, 3000, 'ADULT', 1),
(2, '黄山学生票', '全日制在校学生优惠票', 95.00, 190.00, 5000, 2000, 'STUDENT', 1),
(3, '张家界成人票', '张家界国家森林公园成人票', 225.00, 268.00, 6000, 2000, 'ADULT', 1),
(3, '张家界儿童票', '1.2m-1.5m儿童优惠票', 112.00, 225.00, 3000, 1000, 'CHILD', 1);

-- 示例订单数据（分散在不同日期，用于数据总览报表展示）
INSERT INTO ticket_order (order_no, user_id, scenic_spot_id, total_amount, actual_amount, status, visitor_name, visitor_phone, visit_date, paid_at, created_at) VALUES
('TK202601150001', 1, 1, 338.00, 338.00, 'USED', '李明', '13811112222', '2026-01-15', '2026-01-15 09:15:00', '2026-01-15 09:10:00'),
('TK202601220002', 1, 2, 380.00, 380.00, 'USED', '王芳', '13922223333', '2026-01-22', '2026-01-22 08:30:00', '2026-01-22 08:25:00'),
('TK202601280003', 2, 1, 249.00, 249.00, 'USED', '张伟', '13733334444', '2026-01-28', '2026-01-28 10:00:00', '2026-01-28 09:55:00'),
('TK202602040004', 1, 3, 450.00, 450.00, 'USED', '刘洋', '13644445555', '2026-02-04', '2026-02-04 07:45:00', '2026-02-04 07:40:00'),
('TK202602100005', 2, 1, 169.00, 169.00, 'PAID', '陈静', '13555556666', '2026-02-15', '2026-02-10 11:20:00', '2026-02-10 11:15:00'),
('TK202602150006', 1, 2, 285.00, 285.00, 'PAID', '赵敏', '13466667777', '2026-02-20', '2026-02-15 14:00:00', '2026-02-15 13:55:00'),
('TK202602200007', 1, 1, 507.00, 507.00, 'USED', '孙强', '13377778888', '2026-02-20', '2026-02-20 09:30:00', '2026-02-20 09:25:00'),
('TK202602250008', 2, 3, 337.00, 337.00, 'USED', '周杰', '13288889999', '2026-02-25', '2026-02-25 08:00:00', '2026-02-25 07:55:00'),
('TK202603010009', 1, 1, 169.00, 169.00, 'USED', '吴磊', '13199990000', '2026-03-01', '2026-03-01 10:15:00', '2026-03-01 10:10:00'),
('TK202603050010', 1, 2, 190.00, 190.00, 'USED', '郑华', '13000001111', '2026-03-05', '2026-03-05 12:30:00', '2026-03-05 12:25:00');

-- 订单明细
INSERT INTO ticket_order_item (order_id, ticket_type_id, ticket_name, price, quantity, subtotal) VALUES
(1, 1, '九寨沟成人票', 169.00, 2, 338.00),
(2, 5, '黄山成人票', 190.00, 2, 380.00),
(3, 1, '九寨沟成人票', 169.00, 1, 169.00),
(3, 2, '九寨沟儿童票', 80.00, 1, 80.00),
(4, 7, '张家界成人票', 225.00, 2, 450.00),
(5, 1, '九寨沟成人票', 169.00, 1, 169.00),
(6, 5, '黄山成人票', 190.00, 1, 190.00),
(6, 6, '黄山学生票', 95.00, 1, 95.00),
(7, 1, '九寨沟成人票', 169.00, 3, 507.00),
(8, 7, '张家界成人票', 225.00, 1, 225.00),
(8, 8, '张家界儿童票', 112.00, 1, 112.00),
(9, 1, '九寨沟成人票', 169.00, 1, 169.00),
(10, 5, '黄山成人票', 190.00, 1, 190.00);

-- 检票记录
INSERT INTO check_in_record (order_id, order_no, scenic_spot_id, visitor_name, check_in_time, check_out_time, status, operator_id, gate_no) VALUES
(1, 'TK202602010001', 1, '李明', '2026-02-01 09:30:00', '2026-02-01 17:30:00', 'CHECKED_OUT', 1, 'A1'),
(2, 'TK202602020002', 2, '王芳', '2026-02-02 08:45:00', '2026-02-02 16:45:00', 'CHECKED_OUT', 1, 'B1'),
(3, 'TK202602030003', 1, '张伟', '2026-02-03 10:15:00', '2026-02-03 18:15:00', 'CHECKED_OUT', 2, 'A1'),
(4, 'TK202602040004', 3, '刘洋', '2026-02-04 08:00:00', '2026-02-04 17:00:00', 'CHECKED_OUT', 1, 'C1'),
(7, 'TK202602070007', 1, '孙强', '2026-02-07 09:45:00', '2026-02-07 17:45:00', 'CHECKED_OUT', 1, 'A1'),
(8, 'TK202602080008', 3, '周杰', '2026-02-08 08:15:00', '2026-02-08 16:15:00', 'CHECKED_OUT', 2, 'C1'),
(9, 'TK202602090009', 1, '吴磊', '2026-02-09 10:30:00', '2026-02-09 18:30:00', 'CHECKED_OUT', 1, 'A1'),
(10, 'TK202602100010', 2, '郑华', '2026-02-10 12:45:00', '2026-02-10 19:45:00', 'CHECKED_OUT', 1, 'B1');

-- 更新票种今日已售（用于报表）
UPDATE ticket_type SET sold_today = 120 WHERE id = 1;
UPDATE ticket_type SET sold_today = 85 WHERE id = 2;
UPDATE ticket_type SET sold_today = 45 WHERE id = 5;
UPDATE ticket_type SET sold_today = 62 WHERE id = 7;

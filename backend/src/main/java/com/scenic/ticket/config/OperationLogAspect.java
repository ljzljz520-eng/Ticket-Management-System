// -*- coding: utf-8 -*-
package com.scenic.ticket.config;

import com.scenic.ticket.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * 操作日志 AOP 切面 — 自动记录增删改操作
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService logService;

    /**
     * 匹配所有 Controller 中的 POST / PUT / DELETE 请求映射方法
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void writeOperation() {}

    @Around("writeOperation()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String method = "";
        String ip = "";
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            method = request.getMethod() + " " + request.getRequestURI();
            ip = getClientIp(request);
        }

        // 获取当前用户
        Long userId = null;
        String username = "anonymous";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            username = auth.getName();
            if (auth.getCredentials() instanceof Long) {
                userId = (Long) auth.getCredentials();
            }
        }

        // 操作描述
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String operation = buildOperationDesc(className, methodName);

        // 参数（简化，避免过长）
        String params = Arrays.toString(joinPoint.getArgs());
        if (params.length() > 500) {
            params = params.substring(0, 500) + "...";
        }

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            // 记录成功日志
            logService.saveLogWithDetails(userId, username, operation, method, params, ip, 1, null, duration);

            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - start;

            // 记录失败日志
            logService.saveLogWithDetails(userId, username, operation, method, params, ip, 0, e.getMessage(), duration);

            throw e;
        }
    }

    /**
     * 根据 Controller 和方法名生成操作描述
     */
    private String buildOperationDesc(String className, String methodName) {
        String module = className.replace("Controller", "");
        return switch (module) {
            case "Auth" -> "登录" + (methodName.contains("login") ? "系统" : "操作");
            case "ScenicSpot" -> "景区管理-" + descAction(methodName);
            case "TicketType" -> "票种管理-" + descAction(methodName);
            case "TicketOrder" -> "订单管理-" + descAction(methodName);
            case "CheckIn" -> "检票管理-" + descAction(methodName);
            case "User" -> "用户管理-" + descAction(methodName);
            default -> module + "-" + methodName;
        };
    }

    private String descAction(String methodName) {
        if (methodName.contains("create") || methodName.contains("Create")) return "新增";
        if (methodName.contains("update") || methodName.contains("Update")) return "修改";
        if (methodName.contains("delete") || methodName.contains("Delete")) return "删除";
        if (methodName.contains("toggle") || methodName.contains("Toggle")) return "切换状态";
        if (methodName.contains("reset") || methodName.contains("Reset")) return "重置密码";
        if (methodName.contains("pay")) return "支付";
        if (methodName.contains("cancel")) return "取消";
        if (methodName.contains("refund")) return "退款";
        if (methodName.contains("checkIn") || methodName.contains("check")) return "检票";
        if (methodName.contains("checkOut")) return "出园";
        if (methodName.contains("adjust")) return "调整库存";
        return methodName;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

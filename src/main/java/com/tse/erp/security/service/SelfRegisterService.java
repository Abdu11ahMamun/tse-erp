package com.tse.erp.security.service;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.User;
import com.tse.erp.module.admin.service.UserService;
import com.tse.erp.security.model.SelfRegisterRequest;
import com.tse.erp.security.model.SelfRegisterResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SelfRegisterService {

    private static final long WINDOW_MS = 60L * 60L * 1000L;

    private final UserService userService;

    @Value("${security.self-register.enabled:false}")
    private boolean enabled;

    @Value("${security.self-register.auto-activate:false}")
    private boolean autoActivate;

    @Value("${security.self-register.max-per-ip-per-hour:5}")
    private int maxPerIp;

    @Value("${security.self-register.default-bg-id:0}")
    private long defaultBgId;

    @Value("${security.self-register.default-bu-id:0}")
    private long defaultBuId;

    @Value("${security.self-register.default-role-id:0}")
    private long defaultRoleId;

    @Value("${security.self-register.default-user-type-id:0}")
    private long defaultUserTypeId;

    private final Map<String, Deque<Long>> hits = new HashMap<>();

    public SelfRegisterResponse register(SelfRegisterRequest req,
                                         HttpServletRequest http) {

        // Feature off hole endpoint-i nai moto
        if (!enabled) {
            throw new ResourceNotFoundException(
                    "Self registration is not available");
        }
        if (defaultBgId <= 0 || defaultBuId <= 0
                || defaultRoleId <= 0 || defaultUserTypeId <= 0) {
            throw new BadRequestException(
                    "Self registration is not configured");
        }

        checkRate(clientIp(http));

        if (req.getPassword() == null
                || !req.getPassword().equals(req.getConfirmPassword())) {
            throw new BadRequestException(
                    "Password and Confirm Password do not match");
        }

        // Shudhu allowed field copy hoy. Role/BU/status server theke
        User u = new User();
        u.setUserName(req.getUserName());
        u.setFullName(req.getFullName());
        u.setEmail(req.getEmail());
        u.setMobileNo(req.getMobileNo());
        u.setPassword(req.getPassword());
        u.setBgId(defaultBgId);
        u.setBuId(defaultBuId);
        u.setRoleId(defaultRoleId);
        u.setUserTypeId(defaultUserTypeId);
        u.setStatus(autoActivate ? 1 : 0);

        // Validation (username, email, password rule, unique) UserService-e ache
        User saved = userService.createUser(u);

        return new SelfRegisterResponse(
                saved.getUserName(),
                autoActivate,
                autoActivate
                        ? "Account created. You can log in now."
                        : "Account created. An administrator must activate it before you can log in.");
    }

    // IP-wise ghontay max N ta
    private synchronized void checkRate(String ip) {
        long now = System.currentTimeMillis();
        Deque<Long> q = hits.computeIfAbsent(ip, k -> new ArrayDeque<>());
        while (!q.isEmpty() && now - q.peekFirst() > WINDOW_MS) {
            q.pollFirst();
        }
        if (q.size() >= maxPerIp) {
            throw new BadRequestException(
                    "Too many registration attempts. Please try again later.");
        }
        q.addLast(now);
    }

    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
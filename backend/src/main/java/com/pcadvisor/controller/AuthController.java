// AuthController.java
package com.pcadvisor.controller;

import com.pcadvisor.common.Result;
import com.pcadvisor.dto.LoginRequest;
import com.pcadvisor.dto.LoginResponse;
import com.pcadvisor.dto.RegisterRequest;
import com.pcadvisor.entity.OrdinaryUser;
import com.pcadvisor.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // 允许跨域
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return Result.success("登录成功", response);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token != null) {
                userService.logout(token);
            }
            return Result.success("登出成功");
        } catch (Exception e) {
            log.error("登出失败: {}", e.getMessage());
            return Result.error("登出失败");
        }
    }

    @PostMapping("/register")
    public Result<String> register(@Validated @RequestBody RegisterRequest request) {
        try {
            OrdinaryUser user = new OrdinaryUser();
            user.setUserId("U" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
            user.setUserName(request.getUserName());
            user.setUserPhone(request.getUserPhone());
            user.setUserMailbox(request.getUserMailbox());

            boolean success = userService.register(user, request.getPassword());
            if (success) {
                return Result.success("注册成功");
            } else {
                return Result.error("注册失败");
            }
        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        // 检查用户名是否可用
        return Result.success(true);
    }

    @GetMapping("/check-email")
    public Result<Boolean> checkEmail(@RequestParam String email) {
        // 检查邮箱是否可用
        return Result.success(true);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
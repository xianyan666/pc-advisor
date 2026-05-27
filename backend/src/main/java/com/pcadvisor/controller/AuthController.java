// AuthController.java
package com.pcadvisor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pcadvisor.common.Result;
import com.pcadvisor.dto.LoginRequest;
import com.pcadvisor.dto.LoginResponse;
import com.pcadvisor.dto.RegisterRequest;
import com.pcadvisor.entity.OrdinaryUser;
import com.pcadvisor.entity.User;
import com.pcadvisor.mapper.OrdinaryUserMapper;
import com.pcadvisor.mapper.UserMapper;
import com.pcadvisor.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // 允许跨域
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrdinaryUserMapper ordinaryUserMapper;

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

    @PostMapping("/forgot-password/reset")
    public Result<String> resetForgottenPassword(@RequestBody Map<String, Object> body) {
        String account = String.valueOf(body.getOrDefault("account", "")).trim();
        String newPassword = String.valueOf(body.getOrDefault("newPassword", "")).trim();

        if (account.isBlank()) {
            return Result.error("请输入用户名、邮箱或电话号");
        }
        if (newPassword.isBlank()) {
            return Result.error("请输入新密码");
        }
        if (newPassword.length() < 6 || newPassword.length() > 32) {
            return Result.error("新密码长度需在6-32之间");
        }

        User user = userMapper.selectOne(
                new QueryWrapper<User>()
                        .eq("username", account)
                        .or()
                        .eq("email", account)
        );

        if (user == null) {
            OrdinaryUser ordinaryUser = ordinaryUserMapper.selectOne(
                    new QueryWrapper<OrdinaryUser>()
                            .eq("userName", account)
                            .or()
                            .eq("userMailbox", account)
                            .or()
                            .eq("userPhone", account)
            );
            if (ordinaryUser != null) {
                user = userMapper.selectOne(
                        new QueryWrapper<User>().eq("user_id", ordinaryUser.getUserId())
                );
            }
        }

        if (user == null) {
            return Result.error("未找到匹配的账号");
        }

        user.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        userMapper.updateById(user);
        return Result.success("密码重置成功，请使用新密码登录");
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

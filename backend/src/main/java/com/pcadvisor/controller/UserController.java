package com.pcadvisor.controller;

import com.pcadvisor.entity.User;
import com.pcadvisor.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@CrossOrigin // 跨域支持（测试用）
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 测试接口（无需认证）
     */
    @GetMapping("/test")
    public String test() {
        return "测试成功！";
    }

    /**
     * 根据ID查询用户（需认证+权限）
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORDINARY', 'ADMIN')") // 权限校验
    public User getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    /**
     * 新增用户（参数校验+权限）
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public boolean addUser(@Valid @RequestBody User user) { // @Valid触发参数校验
        return userService.saveUser(user);
    }

    /**
     * 查询所有用户
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ORDINARY', 'ADMIN')")
    public List<User> listAll() {
        return userService.listAll();
    }

}
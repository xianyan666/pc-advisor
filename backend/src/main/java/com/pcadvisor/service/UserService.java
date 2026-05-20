package com.pcadvisor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pcadvisor.dto.LoginRequest;
import com.pcadvisor.dto.LoginResponse;
import com.pcadvisor.entity.OrdinaryUser;
import com.pcadvisor.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户Service接口
 */
public interface UserService extends IService<User> {

    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);

    /**
     * 查询所有用户
     */
    List<User> listAll();

    /**
     * 新增用户（事务示例）
     */
    @Transactional(rollbackFor = Exception.class) // 事务注解，异常时回滚
    boolean saveUser(User user);

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户登出
     */
    void logout(String token);

    /**
     * 用户注册
     */
    boolean register(OrdinaryUser user, String password);
}
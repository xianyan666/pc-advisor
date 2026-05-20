package com.pcadvisor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pcadvisor.common.exception.BusinessException;
import com.pcadvisor.dto.LoginRequest;
import com.pcadvisor.dto.LoginResponse;
import com.pcadvisor.entity.OrdinaryUser;
import com.pcadvisor.common.utils.JwtUtil;
import com.pcadvisor.entity.User;
import com.pcadvisor.mapper.OrdinaryUserMapper;
import com.pcadvisor.mapper.UserMapper;
import com.pcadvisor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户Service实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private OrdinaryUserMapper ordinaryUserMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public User getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    public List<User> listAll() {
        return baseMapper.selectAll();
    }

    @Override
    public boolean saveUser(User user) {
        user.setCreateTime(LocalDateTime.now());
        return save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = getByUsername(request.getUsername());

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        String encryptedPassword = encryptPassword(request.getPassword());
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = generateToken(user);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setUserId(user.getUserId());
        response.setRole(user.getRole());

        return response;
    }

    @Override
    public void logout(String token) {
        System.out.println("用户登出，token: " + token);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(OrdinaryUser ordinaryUser, String password) {
        User existingUser = getByUsername(ordinaryUser.getUserName());
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUserId(ordinaryUser.getUserId());
        user.setUsername(ordinaryUser.getUserName());
        user.setPassword(encryptPassword(password));
        user.setEmail(ordinaryUser.getUserMailbox());
        user.setRole("ORDINARY");
        user.setCreateTime(LocalDateTime.now());
        save(user);

        ordinaryUser.setRegisterTime(LocalDateTime.now());

        return ordinaryUserMapper.insert(ordinaryUser) > 0;
    }

    private String encryptPassword(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }

    private String generateToken(User user) {
        String userId = user.getUserId() != null ? user.getUserId() : String.valueOf(user.getId());
        String role = user.getRole() != null ? user.getRole() : "ORDINARY";
        return jwtUtil.generateToken(userId, role, user.getUsername());
    }
}
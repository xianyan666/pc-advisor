package com.pcadvisor.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data // Lombok注解，简化get/set（需确保pom.xml引入lombok，或手动写get/set）
@TableName("sys_user") // 数据库表名，替换为你的实际表名
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId // MyBatis-Plus主键注解
    private Long id;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空") // 非空校验
    @Size(min = 2, max = 20, message = "用户名长度需在2-20之间") // 长度校验
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确") // 邮箱格式校验
    private String email;

    /**
     * 用户ID（业务ID）
     */
    private String userId;

    /**
     * 用户角色
     */
    private String role;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
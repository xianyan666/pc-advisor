// UserDTO.java - 用户数据传输对象
package com.pcadvisor.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
public class UserDTO {
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "密码必须包含大小写字母和数字，且长度至少8位")
    private String userPassword;

    private String userType;

    // 普通用户信息
    private String userName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String userPhone;

    @Email(message = "邮箱格式不正确")
    private String userMailbox;

    // 管理员信息
    private String adminDept;
    private String adminLevel;
}
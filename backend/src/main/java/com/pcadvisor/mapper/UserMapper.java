package com.pcadvisor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pcadvisor.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User selectByUsername(String username);

    @Select("SELECT * FROM sys_user")
    List<User> selectAll();
}
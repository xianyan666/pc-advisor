package com.pcadvisor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pcadvisor.entity.Administrator;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AdministratorMapper extends BaseMapper<Administrator> {

    /**
     * 根据部门查询管理员
     */
    @Select("SELECT * FROM Administrator WHERE adminDept = #{dept}")
    Administrator selectByDept(@Param("dept") String dept);

    /**
     * 更新管理员信息
     */
    @Update("UPDATE Administrator SET " +
            "adminDept = #{adminDept}, " +
            "adminLevel = #{adminLevel} " +
            "WHERE userID = #{userId}")
    int updateAdminInfo(Administrator admin);
}
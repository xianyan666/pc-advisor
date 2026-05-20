package com.pcadvisor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pcadvisor.dto.UserQueryDTO;
import com.pcadvisor.entity.OrdinaryUser;
import com.pcadvisor.vo.UserDetailVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 普通用户数据访问层接口
 */
@Mapper
public interface OrdinaryUserMapper extends BaseMapper<OrdinaryUser> {

    /**
     * 根据用户ID查询普通用户详细信息（包含关联的User信息）
     */
    @Select("SELECT ou.*, u.userType " +
            "FROM OrdinaryUser ou " +
            "LEFT JOIN User u ON ou.userID = u.userID " +
            "WHERE ou.userID = #{userId}")
    @Results({
            @Result(property = "userId", column = "userID"),
            @Result(property = "userName", column = "userName"),
            @Result(property = "userPhone", column = "userPhone"),
            @Result(property = "userMailbox", column = "userMailbox"),
            @Result(property = "registerTime", column = "RegisterTime"),
            @Result(property = "user", column = "userId",
                    one = @One(select = "com.pcadvisor.mapper.UserMapper.selectById"))
    })
    OrdinaryUser selectUserDetailById(@Param("userId") String userId);

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM OrdinaryUser WHERE userName = #{userName}")
    OrdinaryUser selectByUserName(@Param("userName") String userName);

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM OrdinaryUser WHERE userMailbox = #{email}")
    OrdinaryUser selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM OrdinaryUser WHERE userPhone = #{phone}")
    OrdinaryUser selectByPhone(@Param("phone") String phone);

    /**
     * 检查用户名是否已存在
     */
    @Select("SELECT COUNT(*) FROM OrdinaryUser WHERE userName = #{userName}")
    int countByUserName(@Param("userName") String userName);

    /**
     * 检查邮箱是否已注册
     */
    @Select("SELECT COUNT(*) FROM OrdinaryUser WHERE userMailbox = #{email}")
    int countByEmail(@Param("email") String email);

    /**
     * 检查手机号是否已注册
     */
    @Select("SELECT COUNT(*) FROM OrdinaryUser WHERE userPhone = #{phone}")
    int countByPhone(@Param("phone") String phone);

    /**
     * 分页查询用户列表
     */
    IPage<UserDetailVO> selectUserPage(Page<UserDetailVO> page, @Param("query") UserQueryDTO query);

    /**
     * 批量查询用户信息
     */
    @Select("<script>" +
            "SELECT ou.*, u.userType " +
            "FROM OrdinaryUser ou " +
            "LEFT JOIN User u ON ou.userID = u.userID " +
            "WHERE ou.userID IN " +
            "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    @Results({
            @Result(property = "userId", column = "userID"),
            @Result(property = "userName", column = "userName"),
            @Result(property = "userPhone", column = "userPhone"),
            @Result(property = "userMailbox", column = "userMailbox"),
            @Result(property = "registerTime", column = "RegisterTime")
    })
    List<OrdinaryUser> selectUsersByIds(@Param("userIds") List<String> userIds);

    /**
     * 更新用户最后登录时间
     */
    @Update("UPDATE OrdinaryUser SET lastLoginTime = NOW() WHERE userID = #{userId}")
    int updateLastLoginTime(@Param("userId") String userId);

    /**
     * 更新用户个人信息
     */
    @Update("UPDATE OrdinaryUser SET " +
            "userName = #{userName}, " +
            "userPhone = #{userPhone}, " +
            "userMailbox = #{userMailbox}, " +
            "updateTime = NOW() " +
            "WHERE userID = #{userId}")
    int updateUserInfo(OrdinaryUser user);

    /**
     * 获取用户统计数据
     */
    @Select("SELECT " +
            "COUNT(*) as totalUsers, " +
            "COUNT(CASE WHEN DATE(RegisterTime) = CURDATE() THEN 1 END) as todayNewUsers, " +
            "COUNT(CASE WHEN RegisterTime >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 END) as weekNewUsers " +
            "FROM OrdinaryUser")
    @Results({
            @Result(property = "totalUsers", column = "totalUsers"),
            @Result(property = "todayNewUsers", column = "todayNewUsers"),
            @Result(property = "weekNewUsers", column = "weekNewUsers")
    })
    UserStatistics getUserStatistics();

    /**
     * 统计用户收藏数量
     */
    @Select("SELECT COUNT(*) FROM Collect WHERE userID = #{userId}")
    int countUserCollections(@Param("userId") String userId);

    /**
     * 统计用户发布的测评数量
     */
    @Select("SELECT COUNT(*) FROM Evaluation WHERE userID = #{userId}")
    int countUserEvaluations(@Param("userId") String userId);

    /**
     * 统计用户发布的评论数量
     */
    @Select("SELECT COUNT(*) FROM Comment WHERE userID = #{userId}")
    int countUserComments(@Param("userId") String userId);

    /**
     * 用户统计信息内部类
     */
    class UserStatistics {
        private int totalUsers;
        private int todayNewUsers;
        private int weekNewUsers;

        // getters and setters
        public int getTotalUsers() {
            return totalUsers;
        }

        public void setTotalUsers(int totalUsers) {
            this.totalUsers = totalUsers;
        }

        public int getTodayNewUsers() {
            return todayNewUsers;
        }

        public void setTodayNewUsers(int todayNewUsers) {
            this.todayNewUsers = todayNewUsers;
        }

        public int getWeekNewUsers() {
            return weekNewUsers;
        }

        public void setWeekNewUsers(int weekNewUsers) {
            this.weekNewUsers = weekNewUsers;
        }
    }
}
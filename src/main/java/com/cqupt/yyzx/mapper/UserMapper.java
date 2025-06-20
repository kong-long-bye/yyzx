package com.cqupt.yyzx.mapper;




import com.cqupt.yyzx.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户数据访问接口
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询用户
     */
    User findByUsername(@Param("username") String username);

    /**
     * 根据用户名和密码查询用户
     */
    User findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    /**
     * 插入新用户
     */
    int insert(User user);

    /**
     * 更新最后登录时间
     */
    int updateLastLoginTime(@Param("id") Integer id);
}

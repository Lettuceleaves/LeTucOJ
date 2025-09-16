package com.LetucOJ.sys.repos;

import com.LetucOJ.sys.model.UserDTO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface UserMybatisRepos {

    @Insert("INSERT INTO user (user_name, cnname, password, role, enabled) VALUES (#{userName}, #{cnname}, #{password}, #{role}, #{enabled})")
    Integer saveUserInfo(UserDTO userDTO);

    @Select("SELECT * FROM user WHERE user_name = #{userName}")
    UserDTO getPasswordByUserName(@Param("userName") String userName);

    @Select("SELECT * FROM user WHERE role = #{role}")
    List<UserDTO> getUsersByRole(@Param("role") String role);

    @Update("UPDATE user SET role = 'MANAGER' WHERE user_name = #{userName}")
    Integer setUserToManager(@Param("userName") String userName);

    @Update("UPDATE user SET role = 'USER' WHERE user_name = #{userName}")
    Integer setManagerToUser(@Param("userName") String userName);

    @Update("UPDATE user SET enabled = 1 WHERE user_name = #{userName}")
    Integer activateUser(@Param("userName") String userName);

    @Update("UPDATE user SET enabled = 0 WHERE user_name = #{userName}")
    Integer deactivateUser(@Param("userName") String userName);

    @Update("UPDATE user SET password = #{password}, enabled = 0 WHERE user_name = #{userName}")
    Integer updatePasswordAndLock(@Param("userName") String userName, @Param("password") String password);

    @Select("SELECT user_name  AS userName, problem_name AS name FROM correct")
    List<Map<String, Object>> listCorrect();

    @Select("SELECT name, difficulty FROM problem")
    List<Map<String, Object>> points();
}
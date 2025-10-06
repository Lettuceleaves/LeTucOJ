package com.LetucOJ.sys.repos;

import com.LetucOJ.sys.model.UserInfoDTO;
import com.LetucOJ.sys.model.UserManagerDTO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface UserMybatisRepos {

    @Insert("INSERT INTO user (user_name, cnname, password, role, status) VALUES (#{userName}, #{cnname}, #{password}, #{role}, #{enabled})")
    Integer saveUserInfo(UserManagerDTO userDTO);

    @Select("SELECT * FROM user WHERE user_name = #{userName}")
    UserManagerDTO getPasswordByUserName(@Param("userName") String userName);

    @Select("SELECT * FROM user WHERE role = #{role}")
    List<UserManagerDTO> getUserListByRole(@Param("role") String role);

    @Update("UPDATE user SET role = 'MANAGER' WHERE user_name = #{userName}")
    Integer setUserToManager(@Param("userName") String userName);

    @Update("UPDATE user SET role = 'USER' WHERE user_name = #{userName}")
    Integer setManagerToUser(@Param("userName") String userName);

    @Update("UPDATE user SET status = 1 WHERE user_name = #{userName}")
    Integer activateUser(@Param("userName") String userName);

    @Update("UPDATE user SET status = 0 WHERE user_name = #{userName}")
    Integer deactivateUser(@Param("userName") String userName);

    @Select("SELECT user_name  AS userName, problem_name AS name FROM correct")
    List<Map<String, Object>> listCorrect();

    @Select("SELECT name, difficulty FROM problem")
    List<Map<String, Object>> points();

    @Update("UPDATE user SET cnname = #{cnname}, email = #{email}, phone = #{phone}, description = #{description} where user_name = #{userName}")
    Integer updateUserInfo(UserInfoDTO userDTO);

    @Select("SELECT user_name, cnname, email, phone, description FROM user WHERE user_name = #{userName}")
    UserInfoDTO getUserFullInfo(@Param("userName") String userName);
}
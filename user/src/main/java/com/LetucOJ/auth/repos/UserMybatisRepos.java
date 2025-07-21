package com.LetucOJ.auth.repos;

import com.LetucOJ.auth.model.UserDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserMybatisRepos {

    @Insert("INSERT INTO user (user_name, password, role) VALUES (#{userName}, #{password}, #{role})")
    Integer saveUserInfo(UserDTO userDTO);

    @Select("SELECT * FROM user WHERE user_name = #{userName}")
    UserDTO getPasswordByUserName(String username);
}


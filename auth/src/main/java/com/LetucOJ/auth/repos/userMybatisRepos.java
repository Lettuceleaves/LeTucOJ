package com.LetucOJ.auth.repos;

import com.LetucOJ.auth.model.user;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface userMybatisRepos {
    @Insert("INSERT INTO user (id, user_name, password, role) VALUES (#{id}, #{userName}, #{password}, #{role})")
    int save(user user);

    @Select("SELECT * FROM user WHERE user_name = #{userName}")
    user findByUsername(String userName);

    @Update("UPDATE user SET user_name = #{userName} WHERE id = #{id}")
    int setUserName(int id, String userName);
}

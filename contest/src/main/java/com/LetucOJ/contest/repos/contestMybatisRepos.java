package com.LetucOJ.contest.repos;

import com.LetucOJ.contest.model.contest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface contestMybatisRepos extends BaseMapper<contest> {

    @Delete("DELETE FROM contest WHERE name = #{name}")
    int deleteByName(String name);

    @Select("SELECT * FROM contest WHERE name = #{name}")
    contest getByName(String name);
}

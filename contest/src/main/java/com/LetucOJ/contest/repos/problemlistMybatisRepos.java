package com.LetucOJ.contest.repos;

import com.LetucOJ.contest.model.problemlist;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface problemlistMybatisRepos extends BaseMapper<problemlist> {

    @Delete("DELETE FROM problemlist WHERE contest_id = #{contestId}")
    int deleteProblemById(int contestId);

    @Select("SELECT * FROM problemlist WHERE contest_id = #{contestId}")
    problemlist[] getProblemById(int contestId);
}

package com.LetucOJ.sys.repository;

import com.LetucOJ.common.anno.LanguageConfigDO;
import com.LetucOJ.sys.model.Log;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface MybatisRepos extends BaseMapper<LanguageConfigDO> {
    @Insert("INSERT INTO log (id, trace_id, content) VALUES (#{id}, #{traceId}, #{content})")
    void appendLog(Log log);
}
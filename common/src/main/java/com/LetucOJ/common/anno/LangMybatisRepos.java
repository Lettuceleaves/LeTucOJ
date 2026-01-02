package com.LetucOJ.common.anno;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface LangMybatisRepos {
    @Select("SELECT language, memory_per_run as memPerRun from lang_config where language = #{language} or language = 'total'")
    List<LanguageConfigDO> selectList(String language);
}
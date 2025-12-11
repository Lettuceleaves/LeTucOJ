package com.LetucOJ.common.anno;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface LangMybatisRepos {
    @Select("SELECT lang, mem_per_sum ans MemPerRun from lang_config where lang = #{language} or lang = 'total'")
    List<LanguageConfigDO> selectList(String language);
}
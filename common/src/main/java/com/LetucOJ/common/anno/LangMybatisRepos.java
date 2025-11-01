package com.LetucOJ.common.anno;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface LangMybatisRepos extends BaseMapper<LanguageConfigDO> {
}
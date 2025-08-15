package com.LetucOJ.sys.repos;

import com.LetucOJ.sys.model.ConfigDTO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface SysMybatisRepos {
    @Select("SELECT * FROM sysconfig WHERE label = #{label}")
    ConfigDTO getConfig(String label);
}
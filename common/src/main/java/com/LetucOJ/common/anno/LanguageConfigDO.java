package com.LetucOJ.common.anno;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("lang_config")
public class LanguageConfigDO {
    private String lang;
    private long memPerRun;
}
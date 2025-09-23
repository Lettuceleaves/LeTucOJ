package com.LetucOJ.sys.service;

import com.LetucOJ.common.result.ResultVO;
import org.springframework.stereotype.Service;

@Service
public interface SysService {
    ResultVO getDoc();
    ResultVO updateDoc(byte[] doc);
    ResultVO refreshSql();
}
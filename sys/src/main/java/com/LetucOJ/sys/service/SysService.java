package com.LetucOJ.sys.service;

import com.LetucOJ.common.result.ResultVO;
import org.springframework.stereotype.Service;

@Service
public interface SysService {
    ResultVO<byte[]> getDoc();
    ResultVO<Void> updateDoc(byte[] doc);
    ResultVO<Void> refreshSql();
}
package com.LetucOJ.sys.service;

import com.LetucOJ.sys.model.ResultVO;
import org.springframework.stereotype.Service;

@Service
public interface SysService {
    ResultVO getDoc();
    ResultVO updateDoc(byte[] doc);
}
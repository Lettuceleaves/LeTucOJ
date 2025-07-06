package com.LetucOJ.practice.service;

import com.LetucOJ.practice.model.SqlServiceDTO;
import com.LetucOJ.practice.model.SqlVO;

public interface DBService {
    SqlVO DBServiceSelector(SqlServiceDTO dto);
}

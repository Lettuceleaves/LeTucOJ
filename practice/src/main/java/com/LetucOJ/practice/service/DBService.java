package com.LetucOJ.practice.service;

import com.LetucOJ.practice.model.BasicInfoServiceDTO;
import com.LetucOJ.practice.model.BasicInfoVO;
import com.LetucOJ.practice.model.FullInfoServiceDTO;
import com.LetucOJ.practice.model.FullInfoVO;

public interface DBService {
    BasicInfoVO BasicDBServiceSelector(BasicInfoServiceDTO dto);
    FullInfoVO FullDBServiceSelector(FullInfoServiceDTO dto);
}

package com.LetucOJ.practice.model.VO;

import com.LetucOJ.practice.model.DTO.SubmitRecordDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SubmitRecordListVO {
    List<SubmitRecordDTO> records;
    int amount;
}

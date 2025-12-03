package com.LetucOJ.contest.model.VO;

import com.LetucOJ.contest.model.DTO.SubmitRecordDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SubmitRecordListVO {
    List<SubmitRecordDTO> records;
    int amount;
}

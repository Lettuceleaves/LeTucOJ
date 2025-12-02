package com.LetucOJ.practice.model.VO;

import com.LetucOJ.practice.model.DTO.SubmitRecord;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SubmitRecordListVO {
    List<SubmitRecord> records;
    int amount;
}

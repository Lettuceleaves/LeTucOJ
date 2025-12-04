package com.LetucOJ.contest.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TestTaskDTO {
    String problemName;
    String language;
    String code;
    int caseAmount;
}

package com.LetucOJ.run.model;

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

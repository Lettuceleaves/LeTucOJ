package com.LetucOJ.practice.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.checkerframework.checker.units.qual.A;

@Data
@AllArgsConstructor
public class TestTaskDTO {
    String problemName;
    String language;
    String code;
    int caseAmount;
}

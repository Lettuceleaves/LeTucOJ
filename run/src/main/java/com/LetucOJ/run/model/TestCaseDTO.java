package com.LetucOJ.run.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestCaseDTO {
    private String problemName;
    private String code;
    private String input;
}
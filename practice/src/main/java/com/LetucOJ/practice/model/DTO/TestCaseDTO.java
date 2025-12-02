package com.LetucOJ.practice.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestCaseDTO {
    private String questionName;
    private String code;
    private String input;
}
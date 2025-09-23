package com.LetucOJ.practice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaseInputDTO {
    private String name;
    private String code;
    private String input;
}

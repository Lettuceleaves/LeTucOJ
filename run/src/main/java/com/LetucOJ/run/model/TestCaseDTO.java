package com.LetucOJ.run.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TestCaseDTO {
    private String userCode;
    private List<String> caseFiles;
    private String language;
    private String questionName;
}

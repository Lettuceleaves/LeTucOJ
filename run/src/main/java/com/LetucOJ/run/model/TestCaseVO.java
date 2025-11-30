package com.LetucOJ.run.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TestCaseVO {
    List<String> answer;
    String msg;
}

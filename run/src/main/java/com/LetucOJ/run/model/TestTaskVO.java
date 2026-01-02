package com.LetucOJ.run.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TestTaskVO {
    int failAt;
    String failMsg;
}

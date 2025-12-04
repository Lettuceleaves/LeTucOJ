package com.LetucOJ.practice.model.VO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TestTaskVO {
    int failAt;
    String failMsg;
}

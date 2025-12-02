package com.LetucOJ.practice.model.VO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TestTaskVO {
    List<String> answer;
    String msg;
}

package com.LetucOJ.practice.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class CodeDTO implements Serializable {
    byte service; // 0: run 1: contest 2: test 3: debug
    String code;
    String problemId;
}

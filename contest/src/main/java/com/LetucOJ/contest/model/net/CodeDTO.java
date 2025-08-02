package com.LetucOJ.contest.model.net;

import lombok.Data;

import java.io.Serializable;

@Data
public class CodeDTO implements Serializable {
    byte service; // 0: run 1: contest 2: test 3: debug
    String code;
    String problemName;
    String contestName;
}

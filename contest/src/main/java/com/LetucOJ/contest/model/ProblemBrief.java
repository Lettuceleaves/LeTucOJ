package com.LetucOJ.contest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProblemBrief {
    private String problemName;
    private String problemNickName;
    private String tags;
    private int difficulty;
    private int accepted;
}

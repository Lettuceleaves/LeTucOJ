package com.LetucOJ.contest.model.db;

import lombok.Data;

@Data
public class ProblemStatusDTO {
    boolean ispublic;
    boolean showSolution;
    int count;
}

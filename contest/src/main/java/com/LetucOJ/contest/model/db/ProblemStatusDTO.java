package com.LetucOJ.contest.model.db;

import lombok.Data;

@Data
public class ProblemStatusDTO {
    private boolean ispublic;
    private boolean showSolution;
    private int caseAmount;
}

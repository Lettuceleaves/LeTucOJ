package com.LetucOJ.contest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProblemStatusDTO {
    private boolean ispublic;
    private boolean showSolution;
    private int caseAmount;
}

package com.LetucOJ.contest.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProblemStatusDTO {
    private boolean publicProblem;
    private boolean showSolution;
    private int caseAmount;
}

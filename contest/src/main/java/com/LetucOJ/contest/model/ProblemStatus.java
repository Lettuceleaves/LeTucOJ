package com.LetucOJ.contest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProblemStatus {
    boolean publicProblem;
    boolean showsolution;
    int caseAmount;
    int correct;
}

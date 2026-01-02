package com.LetucOJ.practice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProblemStatus {
    boolean publicProblem;
    boolean showSolution;
    int caseAmount;
    int correct;
}

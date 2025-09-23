package com.LetucOJ.practice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProblemStatusDTO {
    boolean publicProblem;
    boolean showsolution;
    int caseAmount;
    int correct;
}

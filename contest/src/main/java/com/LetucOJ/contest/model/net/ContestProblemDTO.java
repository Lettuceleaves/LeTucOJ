package com.LetucOJ.contest.model.net;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContestProblemDTO {
    String contestName;
    String problemName;
    int score;
}

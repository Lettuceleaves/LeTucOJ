package com.LetucOJ.contest.model.VO;

import com.LetucOJ.contest.model.ProblemBrief;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ContestProblemListVO {
    List<ProblemBrief> problemBriefList;
}

package com.LetucOJ.practice.model.VO;

import com.LetucOJ.practice.model.ProblemBrief;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProblemListVO {
    List<ProblemBrief> problemBriefList;
    int amount;
}

package com.LetucOJ.contest.model.VO;

import com.LetucOJ.contest.model.ContestBrief;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ContestListVO {
    List<ContestBrief> contestInfoDTOList;
}

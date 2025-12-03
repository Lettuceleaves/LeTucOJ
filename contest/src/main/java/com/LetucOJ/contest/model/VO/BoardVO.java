package com.LetucOJ.contest.model.VO;

import com.LetucOJ.contest.model.DTO.BoardDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BoardVO {
    List<BoardDTO> boardList;
    int userAmount;
    int problemAmount;
}

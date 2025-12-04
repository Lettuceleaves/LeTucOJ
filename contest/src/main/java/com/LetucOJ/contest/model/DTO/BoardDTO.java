package com.LetucOJ.contest.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {
    private String contestName;
    private String userName;
    private String nickName;
    private String problemName;
    private int score;
    private int tryCount;
    private int status;
    private LocalDateTime createTime;
    private LocalDateTime acTime;
}

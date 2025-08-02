package com.LetucOJ.contest.model.db;

import lombok.Data;

@Data
public class BoardDTO {
    private String contestName;
    private String userName;
    private int score;
    private int times;
}

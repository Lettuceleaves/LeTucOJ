package com.LetucOJ.contest.model.db;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Locale;

@Data
public class BoardDTO {
    private String contestName;
    private String userName;
    private String userCnname;
    private String problemName;
    private int score;
    private int times;
    private LocalDateTime lastSubmit;
}

package com.LetucOJ.contest.model;

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
    private String userCnname;
    private String problemName;
    private int score;
    private int times;
    private LocalDateTime lastSubmit;
}

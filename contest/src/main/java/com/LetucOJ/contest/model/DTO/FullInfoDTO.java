package com.LetucOJ.contest.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FullInfoDTO {
    private String problemName;
    private String problemNickName;
    private Integer caseAmount;
    private Integer difficulty;
    private String tags;
    private String authors;
    private Date createTime;
    private Date updateTime;
    private String content;
    private Float freq;
    private Boolean publicProblem;
    private String solution;
    private Boolean showSolution;
}
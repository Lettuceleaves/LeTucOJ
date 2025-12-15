package com.LetucOJ.practice.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Problem {
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

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

}
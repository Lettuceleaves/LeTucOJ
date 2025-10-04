package com.LetucOJ.contest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FullInfoDTO {
    private String name;
    private String cnname;
    private Integer caseAmount;
    private Integer difficulty;
    private String tags;
    private String authors;
    private Date createtime;
    private Date updateat;
    private String content;
    private Float freq;
    private Boolean ispublic;
    private String solution;
    private Boolean showsolution;
}
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
    private Boolean publicProblem;
    private String solution;
    private Boolean showsolution;

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

}
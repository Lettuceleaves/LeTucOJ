package com.LetucOJ.practice.model;

import lombok.Data;

@Data
public class SqlDataDTO {
    String id;
    String name;
    long caseAmount;

    public SqlDataDTO(String id, String name, long caseAmount) {
        this.id = id;
        this.name = name;
        this.caseAmount = caseAmount;
    }

    public SqlDataDTO() {
        this.id = "";
        this.name = "";
        this.caseAmount = 0L;
    }
}

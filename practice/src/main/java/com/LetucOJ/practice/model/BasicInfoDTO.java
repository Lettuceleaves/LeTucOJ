package com.LetucOJ.practice.model;

import lombok.Data;

@Data
public class BasicInfoDTO {
    String id;
    String name;
    long caseAmount;

    public BasicInfoDTO(String id, String name, long caseAmount) {
        this.id = id;
        this.name = name;
        this.caseAmount = caseAmount;
    }

    public BasicInfoDTO() {
        this.id = "";
        this.name = "";
        this.caseAmount = 0L;
    }
}

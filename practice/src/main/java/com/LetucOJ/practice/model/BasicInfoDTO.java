package com.LetucOJ.practice.model;

import lombok.Data;

@Data
public class BasicInfoDTO {
    String name;
    String cnname;
    Long caseAmount;

    public BasicInfoDTO(String name, String cnname, long caseAmount) {
        this.name = name;
        this.cnname = cnname;
        this.caseAmount = caseAmount;
    }

    public BasicInfoDTO() {
        this.name = null;
        this.cnname = null;
        this.caseAmount = null;
    }
}

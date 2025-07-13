package com.LetucOJ.practice.model;

import lombok.Data;

@Data
public class BasicInfoServiceDTO {
    String type;
    String subType;
    String name;
    String cnname;
    Long start;
    Long limit;
    BasicInfoDTO data;

    public BasicInfoServiceDTO() {
        this.type = null;
        this.subType = null;
        this.name = null;
        this.cnname = null;
        this.start = null;
        this.limit = null;
        this.data = null;
    }

    public BasicInfoServiceDTO(String type, String subType, String name, String cnname, Long start, Long limit, BasicInfoDTO data) {
        this.type = type;
        this.subType = subType;
        this.name = name;
        this.cnname = null;
        this.start = start;
        this.limit = limit;
        this.data = data;
    }
}
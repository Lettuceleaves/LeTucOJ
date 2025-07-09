package com.LetucOJ.practice.model;

import lombok.Data;

@Data
public class BasicInfoServiceDTO {
    String type;
    String subType;
    String id;
    Long limit;
    BasicInfoDTO data;

    public BasicInfoServiceDTO() {
        this.type = "";
        this.subType = "";
        this.id = "";
        this.limit = 0L;
        this.data = new BasicInfoDTO();
    }

    public BasicInfoServiceDTO(String type, String subType, String id, Long limit, BasicInfoDTO data) {
        this.type = type;
        this.subType = subType;
        this.id = id;
        this.limit = limit;
        this.data = data;
    }
}
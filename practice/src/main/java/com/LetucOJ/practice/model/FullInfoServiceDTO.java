package com.LetucOJ.practice.model;

import lombok.Data;

@Data
public class FullInfoServiceDTO {
    String type;
    String subType;
    String id;
    Long limit;
    FullInfoDTO data;

    public FullInfoServiceDTO() {
        this.type = "";
        this.subType = "";
        this.id = "";
        this.limit = 0L;
        this.data = new FullInfoDTO();
    }

    public FullInfoServiceDTO(String type, String subType, String id, Long limit, FullInfoDTO data) {
        this.type = type;
        this.subType = subType;
        this.id = id;
        this.limit = limit;
        this.data = data;
    }
}

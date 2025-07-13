package com.LetucOJ.practice.model;

import lombok.Data;

@Data
public class FullInfoServiceDTO {
    String type;
    String subType;
    String name;
    String cnname;
    Long limit;
    FullInfoDTO data;

    public FullInfoServiceDTO() {
        this.type = null;
        this.subType = null;
        this.name = null;
        this.cnname = null;
        this.limit = null;
        this.data = null;
    }

    public FullInfoServiceDTO(String type, String subType, String name, String cnname, Long limit, FullInfoDTO data) {
        this.type = type;
        this.subType = subType;
        this.name = name;
        this.cnname = null;
        this.limit = limit;
        this.data = data;
    }
}

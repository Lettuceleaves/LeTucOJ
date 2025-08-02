package com.LetucOJ.practice.model;

import lombok.Data;

@Data
public class FullInfoServiceDTO {
    String name;
    String cnname;
    Long limit;
    FullInfoDTO data;

    public FullInfoServiceDTO() {
        this.name = null;
        this.cnname = null;
        this.limit = null;
        this.data = null;
    }

    public FullInfoServiceDTO(String name, String cnname, Long limit, FullInfoDTO data) {
        this.name = name;
        this.cnname = null;
        this.limit = limit;
        this.data = data;
    }
}

package com.LetucOJ.practice.model;

import lombok.Data;

@Data
public class SqlServiceDTO {
    String type;
    String subType;
    String id;
    Long limit;
    SqlDataDTO data;

    public SqlServiceDTO() {
        this.type = "";
        this.subType = "";
        this.id = "";
        this.limit = 0L;
        this.data = new SqlDataDTO();
    }

    public SqlServiceDTO(String type, String subType, String id, Long limit, SqlDataDTO data) {
        this.type = type;
        this.subType = subType;
        this.id = id;
        this.limit = limit;
        this.data = data;
    }
}
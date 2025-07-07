package com.LetucOJ.practice.model;

import lombok.Data;

@Data
public class SqlVO {
    private byte type; // 0: error 1: select 2: insert 3: update 4: delete
    private SqlDataDTO data;
    private String message;

    public SqlVO() {
        this.type = 0;
        this.data = null;
        this.message = "";
    }

    public SqlVO(byte type, SqlDataDTO data, String message) {
        this.type = type;
        this.data = data;
        this.message = message;
    }
}

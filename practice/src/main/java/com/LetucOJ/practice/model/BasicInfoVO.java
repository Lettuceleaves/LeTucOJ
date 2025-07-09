package com.LetucOJ.practice.model;

import lombok.Data;

@Data
public class BasicInfoVO {
    private byte type; // 0: error 1: select 2: insert 3: update 4: delete
    private BasicInfoDTO data;
    private String message;

    public BasicInfoVO() {
        this.type = 0;
        this.data = null;
        this.message = "";
    }

    public BasicInfoVO(byte type, BasicInfoDTO data, String message) {
        this.type = type;
        this.data = data;
        this.message = message;
    }
}

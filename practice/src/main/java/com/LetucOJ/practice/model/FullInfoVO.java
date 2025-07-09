package com.LetucOJ.practice.model;

import lombok.Data;

@Data
public class FullInfoVO {

    private byte type; // 0: error 1: select 2: insert 3: update 4: delete
    private FullInfoDTO data;
    private String message;

    public FullInfoVO() {
        this.type = 0;
        this.data = null;
        this.message = "";
    }

    public FullInfoVO(byte type, FullInfoDTO data, String message) {
        this.type = type;
        this.data = data;
        this.message = message;
    }
}

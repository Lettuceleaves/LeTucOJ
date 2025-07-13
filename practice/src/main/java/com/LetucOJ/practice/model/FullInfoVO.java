package com.LetucOJ.practice.model;

import lombok.Data;

@Data
public class FullInfoVO {

    private Byte type; // 0: error 1: select 2: insert 3: update 4: delete
    private FullInfoDTO data;
    private String message;

    public FullInfoVO() {
        this.type = null;
        this.data = null;
        this.message = null;
    }

    public FullInfoVO(byte type, FullInfoDTO data, String message) {
        this.type = type;
        this.data = data;
        this.message = message;
    }
}

package com.LetucOJ.practice.model;

import lombok.Data;

import java.util.List;

@Data
public class BasicInfoVO {
    private Byte type; // 0: error 1: select 2: insert 3: update 4: delete
    private List<BasicInfoDTO> data;
    private String message;

    public BasicInfoVO() {
        this.type = null;
        this.data = null;
        this.message = null;
    }

    public BasicInfoVO(byte type, List<BasicInfoDTO> data, String message) {
        this.type = type;
        this.data = data;
        this.message = message;
    }
}

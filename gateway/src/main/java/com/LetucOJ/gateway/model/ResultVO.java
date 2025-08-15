package com.LetucOJ.gateway.model;

import lombok.Data;

@Data
public class ResultVO {
    private int status; // 0: success, 1: fail, 2: error
    private Object data;
    private String error;

    public ResultVO() {}

    public ResultVO(int status, Object data, String error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }
}

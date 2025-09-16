package com.LetucOJ.sys.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
public class ResultVO {
    private int status; // 0: success, 1: fail, 2: error
    private Object data;
    private String error;

    public ResultVO(int status, Object data, String error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }
}

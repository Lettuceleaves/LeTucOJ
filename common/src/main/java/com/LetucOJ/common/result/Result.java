package com.LetucOJ.common.result;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Result<T> {
    public static final String SUCCESS_CODE = "0";
    private String code;
    private String message;
    private T data;

    private String requestId;
    public boolean isSuccess() {
        return SUCCESS_CODE.equals(code);
    }

    public boolean isFail() {
        return !isSuccess();
    }
}

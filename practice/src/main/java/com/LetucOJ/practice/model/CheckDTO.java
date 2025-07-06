package com.LetucOJ.practice.model;

import lombok.Data;

@Data
public class CheckDTO {
    private byte status; // 0: pass 1: fail 2: error
    private long caseIndex;
    private String message;
    private boolean hideIndexMessage;
    private boolean hideErrorMessage;

    public CheckDTO(byte b, String s) {
        this.status = b;
        this.caseIndex = -1;
        this.message = s;
        this.hideIndexMessage = false;
        this.hideErrorMessage = false;
    }
}

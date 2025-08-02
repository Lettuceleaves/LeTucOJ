package com.LetucOJ.contest.model.method;

import lombok.Data;

@Data
public class CheckDTO {
    private byte status; // 0: pass 1: fail 2: error
    private long caseIndex;
    private String message;
    private boolean hideIndexMessage;
    private boolean hideErrorMessage;
    private int index;

    public CheckDTO(byte b, String s, int index) {
        this.status = b;
        this.caseIndex = -1;
        this.message = s;
        this.hideIndexMessage = false;
        this.hideErrorMessage = false;
        this.index = index;
    }
}

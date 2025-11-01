package com.LetucOJ.common.anno;

public enum SubmitStatus {
    COMMITING("COMMITING"),
    COMMITED("COMMITED");

    private final String message;

    SubmitStatus(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}

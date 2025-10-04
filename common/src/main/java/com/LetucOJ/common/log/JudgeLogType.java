package com.LetucOJ.common.log;

public enum JudgeLogType {

    ACCEPT_("ACCEPT_"), // 回答正确
    WRONG__("WRONG__"), // 回答错误
    COMPILE("COMPILE"), // 编译错误
    RUNTIME("RUNTIME"), // 运行错误
    TIMEOUT("TIMEOUT"); // 运行超时

    private final String message;

    JudgeLogType(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}

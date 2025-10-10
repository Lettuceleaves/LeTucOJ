package com.LetucOJ.common.log;

public enum LogLevel {
    INFO("INFO"),   // 信息
    WARN("WARN"),   // 警告
    ERROR("ERROR"); // 错误

    private final String message;

    LogLevel(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}

package com.LetucOJ.common.log;

public enum Type {

    CLIENT("CLIENT___"), // 客户端日志
    SERVER("SERVER___"), // 服务端日志
    EXTERNAL("EXTERNAL_"), // 第三方日志
    DEPENDENT("DEPENDENT"); // 依赖方日志

    private final String message;

    Type(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}

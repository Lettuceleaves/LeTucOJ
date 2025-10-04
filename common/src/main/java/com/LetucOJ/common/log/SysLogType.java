package com.LetucOJ.common.log;

public enum SysLogType {

    CLIENT___("CLIENT___"), // 客户端日志
    SERVER___("SERVER___"), // 服务端日志
    EXTERNAL_("EXTERNAL_"), // 第三方日志
    DEPENDENT("DEPENDENT"); // 依赖方日志

    private final String message;

    SysLogType(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}

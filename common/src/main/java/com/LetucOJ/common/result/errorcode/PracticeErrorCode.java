package com.LetucOJ.common.result.errorcode;

public enum PracticeErrorCode implements ErrorCode {
    // ========== 模块错误码 客户端错误 ==========
    CLIENT_ERROR("A040001", "用户端错误"),

    // ========== 模块错误码 系统执行出错 ==========
    SERVICE_ERROR("B040001", "系统执行出错"),
    NO_RECORD_FOUND("B040002", "暂无历史记录"),
    NOT_PUBLIC("B040003", "非公开题目"),

    // ========== 模块错误码 调用第三方服务出错 ==========
    REMOTE_ERROR("C040001", "调用第三方服务出错");

    private final String code;

    private final String message;

    PracticeErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}

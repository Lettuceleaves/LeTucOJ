package com.LetucOJ.common.result.errorcode;

public enum RunErrorCode implements ErrorCode {
    // ========== 模块错误码 客户端错误 ==========
    CLIENT_ERROR("A050001", "用户端错误"),

    // ========== 模块错误码 系统执行出错 ==========
    SERVICE_ERROR("B050001", "系统执行出错"),
    VALIDATE_ERROR("B050002", "语言缺失或配置错误"),

    // ========== 模块错误码 调用第三方服务出错 ==========
    REMOTE_ERROR("C050001", "调用第三方服务出错"),
    WRONG_ANSWER("B010005", "答案错误"),
    COMPILE_ERROR("B010006", "编译错误"),
    RUNTIME_ERROR("B010007", "运行时错误"),
    OUT_OF_TIME("B010008", "运行超时");

    private final String code;

    private final String message;

    RunErrorCode(String code, String message) {
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

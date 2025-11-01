package com.LetucOJ.gateway.result.errorcode;

public enum GatewayErrorCode implements ErrorCode {
    // ========== 模块错误码 客户端错误 ==========
    CLIENT_ERROR("A030001", "用户端错误"),

    // ========== 模块错误码 系统执行出错 ==========
    SERVICE_ERROR("B030001", "系统执行出错"),
    USER_BLOCKED("B030002", "用户被封禁"),
    LOGIN_EXPIRED("B030003", "登录过期，请重新登录"),

    // ========== 模块错误码 调用第三方服务出错 ==========
    REMOTE_ERROR("C030001", "调用第三方服务出错");

    private final String code;

    private final String message;

    GatewayErrorCode(String code, String message) {
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

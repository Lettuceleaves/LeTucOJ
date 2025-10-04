package com.LetucOJ.common.result.errorcode;

public enum SysErrorCode implements ErrorCode {
    // ========== 模块错误码 客户端错误 ==========
    CLIENT_ERROR("A060001", "用户端错误"),

    // ========== 模块错误码 系统执行出错 ==========
    SERVICE_ERROR("B060001", "系统执行出错"),

    // ========== 模块错误码 调用第三方服务出错 ==========
    REMOTE_ERROR("C060001", "调用第三方服务出错");

    private final String code;

    private final String message;

    SysErrorCode(String code, String message) {
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

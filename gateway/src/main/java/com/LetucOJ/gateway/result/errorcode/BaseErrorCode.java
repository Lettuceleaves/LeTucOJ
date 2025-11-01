
package com.LetucOJ.gateway.result.errorcode;

public enum BaseErrorCode implements ErrorCode {

    // ========== 一级宏观错误码 客户端错误 ==========
    CLIENT_ERROR("A010001", "用户端错误"),
    EMPTY_DATA("A010002", "数据不能为空"),
    NEED_LOGIN("A010003", "需要登录"),

    // ========== 一级宏观错误码 系统执行出错 ==========
    SERVICE_ERROR("B010001", "系统执行出错"),
    PROBLEM_NOT_EXIST("B010002", "题目不存在"),
    NO_CASE_EXIST("B010003", "没有测试案例"),
    NOT_SUPPORT("B010004", "功能暂不开放"),
    WRONG_ANSWER("B010005", "答案错误"),
    COMPILE_ERROR("B010006", "编译错误"),
    RUNTIME_ERROR("B010007", "运行时错误"),
    OUT_OF_TIME("B010008", "运行超时"),

    // ========== 一级宏观错误码 调用第三方服务出错 ==========
    REMOTE_ERROR("C010001", "调用第三方服务出错");

    private final String code;

    private final String message;

    BaseErrorCode(String code, String message) {
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

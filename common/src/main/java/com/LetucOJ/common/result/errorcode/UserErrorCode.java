package com.LetucOJ.common.result.errorcode;

public enum UserErrorCode implements ErrorCode {
    // ========== 模块错误码 客户端错误 ==========
    CLIENT_ERROR("A070001", "用户端错误"),
    EMPTY_PARAMETER("A070002", "参数为空"),
    FILE_TOO_LARGE("A070003", "文件过大"),
    PARAM_FORMAT_ERROR("A070004", "参数格式错误"),
    USERNAME_ALREADY_EXISTS("A070005", "用户名已存在"),

    // ========== 模块错误码 系统执行出错 ==========
    SERVICE_ERROR("B070001", "系统执行出错"),
    REGISTER_FAILED("B070002", "注册失败"),
    NAME_OR_CODE_WRONG("B070003", "账号(请使用英文名)或密码不正确"),
    NOT_ENABLE("B070004", "账号未激活"),
    NO_USER("B070005", "没有用户"),
    NO_MANAGER("B070006", "没有管理员"),
    NO_RANK("B070007", "暂无排名"),
    NO_HEATMAP("B070008", "这个人很懒，今年一题没写"),
    NO_HEADPORTRAIT("B070009", "没有头像"),
    NO_BACKGROUND("B070010", "没有背景图"),

    // ========== 模块错误码 调用第三方服务出错 ==========
    REMOTE_ERROR("C070001", "调用第三方服务出错");

    private final String code;

    private final String message;

    UserErrorCode(String code, String message) {
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

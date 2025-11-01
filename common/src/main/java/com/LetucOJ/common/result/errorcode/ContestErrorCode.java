package com.LetucOJ.common.result.errorcode;

public enum ContestErrorCode implements ErrorCode {

    // ========== 模块错误码 客户端错误 ==========
    CLIENT_ERROR("A020001", "用户端错误"),
    EMPTY_DATA("A020002", "数据不能为空"),
    INVALID_PARAM("A020003", "参数不合法"),

    // ========== 模块错误码 系统执行出错 ==========
    SERVICE_ERROR("B020001", "系统执行出错"),
    CONTEST_FINISHED("B020002", "竞赛已结束"),
    CONTEST_NOT_START("B020003", "竞赛未开始"),
    CONTEST_NOT_PUBLIC("B020004", "竞赛未公开"),
    NO_CONTEST("B020005", "暂无竞赛"),
    NO_PROBLEM_IN_CONTEST("B020007", "竞赛中暂无题目"),
    USER_NOT_IN_CONTEST("B020008", "未参加竞赛"),
    EMPTY_BOARD("B020009", "榜单为空"),
    WRONG_ANSWER("B020010", "答案错误"),
    CONTEST_NOT_EXIST("B020011", "赛事不存在"),
    USER_NOT_ATTEND("B020012", "用户未参加该竞赛"),
    // ========== 模块错误码 调用第三方服务出错 ==========
    REMOTE_ERROR("C020001", "调用第三方服务出错");

    private final String code;

    private final String message;

    ContestErrorCode(String code, String message) {
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

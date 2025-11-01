
package com.LetucOJ.gateway.result;

import com.LetucOJ.gateway.result.errorcode.BaseErrorCode;
import com.LetucOJ.gateway.result.errorcode.ErrorCode;

public final class Result {

    public static ResultVO<Void> success() {
        return new ResultVO<Void>()
                .setCode(ResultVO.SUCCESS_CODE);
    }

    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<T>().setCode(ResultVO.SUCCESS_CODE).setData(data);
    }

    public static ResultVO<Void> failure() {
        return new ResultVO<Void>()
                .setCode(BaseErrorCode.SERVICE_ERROR.code())
                .setMessage(BaseErrorCode.SERVICE_ERROR.message());
    }

    public static ResultVO<Void> failure(ErrorCode errorCode) {
        return new ResultVO<Void>()
                .setCode(errorCode.code())
                .setMessage(errorCode.message());
    }

    public static <T> ResultVO<T> failure(ErrorCode errorCode, T data) {
        return new ResultVO<T>()
                .setCode(errorCode.code())
                .setData(data)
                .setMessage(errorCode.message());
    }
}

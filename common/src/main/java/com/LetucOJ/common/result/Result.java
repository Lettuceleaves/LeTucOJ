
package com.LetucOJ.common.result;

import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.exception.AbstractException;
import com.LetucOJ.common.result.errorcode.ErrorCode;

import java.util.Optional;

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

    public static ResultVO<Void> failure(AbstractException abstractException) {
        String errorCode = Optional.ofNullable(abstractException.getErrorCode())
                .orElse(BaseErrorCode.SERVICE_ERROR.code());
        String errorMessage = Optional.ofNullable(abstractException.getErrorMessage())
                .orElse(BaseErrorCode.SERVICE_ERROR.message());
        return new ResultVO<Void>()
                .setCode(errorCode)
                .setMessage(errorMessage);
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

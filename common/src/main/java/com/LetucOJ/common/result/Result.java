
package com.LetucOJ.common.result;

import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.result.errorcode.ErrorCode;
import com.LetucOJ.common.trace.TraceContext;

public final class Result {

    public static ResultVO<Void> success() {
        return new ResultVO<Void>()
                .setCode(ResultVO.SUCCESS_CODE)
                .setTaskId(TraceContext.getTraceId());
    }

    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<T>().setCode(ResultVO.SUCCESS_CODE).setData(data).setTaskId(TraceContext.getTraceId());
    }

    public static ResultVO<Void> failure() {
        return new ResultVO<Void>()
                .setCode(BaseErrorCode.SERVICE_ERROR.code())
                .setMessage(BaseErrorCode.SERVICE_ERROR.message())
                .setTaskId(TraceContext.getTraceId());
    }

    public static ResultVO<Void> failure(ErrorCode errorCode) {
        return new ResultVO<Void>()
                .setCode(errorCode.code())
                .setMessage(errorCode.message())
                .setTaskId(TraceContext.getTraceId());
    }

    public static <T> ResultVO<T> failure(ErrorCode errorCode, T data) {
        return new ResultVO<T>()
                .setCode(errorCode.code())
                .setData(data)
                .setMessage(errorCode.message())
                .setTaskId(TraceContext.getTraceId());
    }
}

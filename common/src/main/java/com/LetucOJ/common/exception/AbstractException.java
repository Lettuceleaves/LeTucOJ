
package com.LetucOJ.common.exception;

import com.LetucOJ.common.result.errorcode.ErrorCode;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Getter
public abstract class AbstractException extends RuntimeException {

    public final String errorCode;

    public final String errorMessage;

    public AbstractException(String message, Throwable throwable, ErrorCode errorCode) {
        super(message, throwable);
        this.errorCode = errorCode.code();
        this.errorMessage = Optional.ofNullable(StringUtils.hasLength(message) ? message : null).orElse(errorCode.message());
    }
}

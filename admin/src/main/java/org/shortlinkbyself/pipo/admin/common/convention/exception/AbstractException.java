package org.shortlinkbyself.pipo.admin.common.convention.exception;

import lombok.Getter;
import org.shortlinkbyself.pipo.admin.common.convention.errorcode.IErrorCode;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Getter
public abstract class AbstractException extends RuntimeException{
    public final String errorCode;

    public final String errorMessage;

    public AbstractException(String message, Throwable throwable, IErrorCode errorCode) {

        super(message, throwable);
        this.errorCode = errorCode.code();
        this.errorMessage = Optional.ofNullable(message)
                .filter(StringUtils::hasLength)
                .orElse(errorCode.message());
    }
}

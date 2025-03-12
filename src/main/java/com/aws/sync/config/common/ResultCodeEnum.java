package com.aws.sync.config.common;
import lombok.Getter;

import java.util.Deque;
import java.util.Stack;

/**
 * Result class enumeration
 */
@Getter
public enum ResultCodeEnum {
    SUCCESS(true,20000,"success"),
    UNKNOWN_ERROR(false,20001,"unknown error"),
    PARAM_ERROR(false,20002,"parameter error"),
    NULL_POINT(false, 20003, "NullPoint error"),
    INDEX_OUT_OF_BOUNDS(false, 20004, "IndexOutOfBounds error"),
    FILE_TYPE_ERROR(false, 20005, "File type error"),
    IO_EXCEPTION(false, 20006, "IO error"),
    ERR_AVATAR_NOT_FOUND(false, 20007, "IError code for avatar not found"),
    ;

    // Response success or not
    private Boolean success;
    // Response status code
    private Integer code;
    // Response message
    private String message;



    ResultCodeEnum(boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }
}

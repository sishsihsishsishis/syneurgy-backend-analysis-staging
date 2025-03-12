package com.aws.sync.advice;

import com.aws.sync.config.common.ResultCodeEnum;
import lombok.Data;

@Data
public class SystemException extends RuntimeException{

    private Boolean success;
    private Integer code;
    private String message;

    public SystemException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
        this.success = resultCodeEnum.getSuccess();
        this.message = resultCodeEnum.getMessage();
    }

    public SystemException(ResultCodeEnum resultCodeEnum, String customMessage) {
        super(customMessage);
        this.success = resultCodeEnum.getSuccess();
        this.code = resultCodeEnum.getCode();
        this.message = customMessage;
    }

}

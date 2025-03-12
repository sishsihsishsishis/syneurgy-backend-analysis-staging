package com.aws.sync.advice;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.config.common.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandlerAdvice {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public RestResult error(Exception e) {
        e.printStackTrace();
        log.error("Global exception capture:" + e);
        return RestResult.fail().message(e.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseBody
    //NullPoint handling method
    public RestResult error(NullPointerException e) {
        e.printStackTrace();
        log.error("Global exception capture:" + e);
        return RestResult.setResult(ResultCodeEnum.NULL_POINT);
    }

    //IndexOutOfBounds handling method
    @ExceptionHandler(IndexOutOfBoundsException.class)
    @ResponseBody
    public RestResult error(IndexOutOfBoundsException e) {
        e.printStackTrace();
        log.error("Global exception capture:" + e);
        return RestResult.setResult(ResultCodeEnum.INDEX_OUT_OF_BOUNDS);
    }

    @ExceptionHandler(SystemException.class)
    @ResponseBody
    public RestResult systemExceptionHandler(SystemException e){
        e.printStackTrace();
        log.error("System exception capture:" + e);
        return RestResult.setResult(ResultCodeEnum.FILE_TYPE_ERROR);
    }
}

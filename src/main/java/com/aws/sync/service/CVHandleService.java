package com.aws.sync.service;

import com.aws.sync.config.common.RestResult;

import java.io.IOException;

public interface CVHandleService {
    RestResult handleCV(Long meetingID) throws Exception;

    RestResult handleScore(Long meetingID, int coefficientBody,int coefficientBehaviour, int coefficientTotal) throws Exception;
    RestResult handlePartCV(Long meetingID, Integer part) throws Exception;

    RestResult removePartCV(Long meetingID, Integer part) throws Exception;
}

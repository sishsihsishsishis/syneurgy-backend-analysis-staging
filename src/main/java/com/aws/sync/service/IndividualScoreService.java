package com.aws.sync.service;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.IndividualScore;
import com.baomidou.mybatisplus.extension.service.IService;


public interface IndividualScoreService extends IService<IndividualScore> {

    RestResult getUserScore(Long meetingID);
}

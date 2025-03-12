package com.aws.sync.service;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.PosNegRate;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PosAndNegRateService extends IService<PosNegRate> {

    RestResult getPositiveAndNegativeRateByMeetingId(Long meetingID);
}

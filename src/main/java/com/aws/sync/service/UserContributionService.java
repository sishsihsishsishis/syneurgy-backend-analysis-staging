package com.aws.sync.service;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.UserContribution;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserContributionService extends IService<UserContribution> {

    RestResult getUserContributionByMeetingId(Long meetingID);
}

package com.aws.sync.service.impl;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.UserContribution;
import com.aws.sync.mapper.UserContributionMapper;
import com.aws.sync.service.UserContributionService;
import com.aws.sync.vo.UserContributionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserContributionServiceImpl extends ServiceImpl<UserContributionMapper, UserContribution> implements UserContributionService {

    @Override
    public RestResult getUserContributionByMeetingId(Long meetingID) {
        LambdaQueryWrapper<UserContribution> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserContribution::getMeeting_id,meetingID);

        List<UserContributionVO> userContributionVOList = list(lambdaQueryWrapper)
                .stream()
                .map(userContribution -> {
                    UserContributionVO u = new UserContributionVO();
                    BeanUtils.copyProperties(userContribution,u);
                    return u;
                })
                .collect(Collectors.toList());


        return RestResult.success().data(userContributionVOList);
    }
}

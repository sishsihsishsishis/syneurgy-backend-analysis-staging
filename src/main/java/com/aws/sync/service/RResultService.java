package com.aws.sync.service;

import com.aws.sync.vo.csv.AllUser;
import com.aws.sync.vo.csv.RMean;
import com.aws.sync.entity.RResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface RResultService extends IService<RResult> {
    int insertR(List<RResult> rResults);

    void addOneByOne(List<RResult> rResults);
    List<RMean> findMean(Long meetingID);

    List<AllUser> findUser(Long meetingID);
}

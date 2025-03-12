package com.aws.sync.service;

import com.aws.sync.entity.IndividualSyncR;
import com.aws.sync.vo.IndividualVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IndividualRService extends IService<IndividualSyncR> {
    List<Double> findTimeLine(Long meetingID);

    List<String> findUserList(Long meetingID);

    List<IndividualVO> findIndividualByUser(Long meetingID, String s);
}

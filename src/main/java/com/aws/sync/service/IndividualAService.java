package com.aws.sync.service;

import com.aws.sync.entity.IndividualSyncA;
import com.aws.sync.vo.IndividualVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IndividualAService extends IService<IndividualSyncA> {

    List<IndividualVO> findIndividualA(Long meetingID);

    List<Double> findTimeLine(Long meetingID);

    List<String> findUserList(Long meetingID);

    List<IndividualVO> findIndividualByUser(Long meetingID, String userName);
}

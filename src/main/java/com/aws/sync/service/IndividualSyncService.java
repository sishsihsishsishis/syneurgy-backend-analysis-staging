package com.aws.sync.service;

import com.aws.sync.entity.IndividualSync;
import com.aws.sync.vo.IndividualAllVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IndividualSyncService extends IService<IndividualSync> {
    void addOneByOne(List<IndividualSync> individualSyncAll);

    List<String> findUserList(Long meetingID);

    List<IndividualAllVO> findIndividualByUser(Long meetingID, String user);
}

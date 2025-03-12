package com.aws.sync.service;

import com.aws.sync.vo.csv.AMean;
import com.aws.sync.vo.csv.AllUser;
import com.aws.sync.entity.AResult;
import com.aws.sync.vo.DataAVO;
import com.baomidou.mybatisplus.extension.service.IService;


import java.util.List;

public interface AResultService extends IService<AResult> {
    int insertA(List<AResult> aResults);

    void addOneByOne(List<AResult> aResults);
    List<AMean> findMean(Long meetingID);

    List<AllUser> findUser(Long meetingID);

    List<Long> findTime(Long meetingID);

    List<DataAVO> findData(Long meetingID);
}

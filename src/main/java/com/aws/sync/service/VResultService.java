package com.aws.sync.service;

import com.aws.sync.vo.csv.AllUser;
import com.aws.sync.vo.csv.VMean;
import com.aws.sync.entity.VResult;
import com.aws.sync.vo.DataVVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface VResultService extends IService<VResult> {
    int insertV(List<VResult> vResults);

    void addOneByOne(List<VResult> vResults);

    List<VMean> findMean(Long meetingID);

    List<AllUser> findUser(Long meetingID);

    List<DataVVO> findData(Long meetingID);
}

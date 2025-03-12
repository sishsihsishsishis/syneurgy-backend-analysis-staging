package com.aws.sync.service.impl;

import com.aws.sync.entity.IndividualSyncV;
import com.aws.sync.mapper.IndividualAMapper;
import com.aws.sync.mapper.IndividualVMapper;
import com.aws.sync.service.IndividualVService;
import com.aws.sync.vo.IndividualVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class IndividualVServiceImpl extends ServiceImpl<IndividualVMapper, IndividualSyncV> implements IndividualVService {
    @Resource
    IndividualVMapper individualVMapper;

    @Override
    public List<Double> findTimeLine(Long meetingID) {
        return individualVMapper.selectTimeLine(meetingID);
    }

    @Override
    public List<String> findUserList(Long meetingID) {
        return individualVMapper.selectUserList(meetingID);
    }

    @Override
    public List<IndividualVO> findIndividualByUser(Long meetingID, String username) {
        return individualVMapper.selectIndividualByUser(meetingID,username);
    }
}

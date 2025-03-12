package com.aws.sync.service.impl;

import com.aws.sync.entity.IndividualSyncR;
import com.aws.sync.mapper.IndividualRMapper;
import com.aws.sync.service.IndividualRService;
import com.aws.sync.vo.IndividualVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class IndividualRServiceImpl extends ServiceImpl<IndividualRMapper, IndividualSyncR> implements IndividualRService {
    @Resource
    IndividualRMapper individualRMapper;

    @Override
    public List<Double> findTimeLine(Long meetingID) {
        return individualRMapper.selectTimeLine(meetingID);
    }

    @Override
    public List<String> findUserList(Long meetingID) {
        return individualRMapper.selectUserList(meetingID);
    }

    @Override
    public List<IndividualVO> findIndividualByUser(Long meetingID, String s) {
        return individualRMapper.selectIndividualByUser(meetingID,s);
    }
}

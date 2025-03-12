package com.aws.sync.service.impl;

import com.aws.sync.entity.IndividualSyncA;
import com.aws.sync.mapper.IndividualAMapper;
import com.aws.sync.service.IndividualAService;
import com.aws.sync.vo.IndividualVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class IndividualAServiceImpl extends ServiceImpl<IndividualAMapper, IndividualSyncA> implements IndividualAService {
    @Resource
    IndividualAMapper individualAMapper;

    @Override
    public List<IndividualVO> findIndividualA(Long meetingID) {
        return individualAMapper.findIndividual(meetingID);
    }

    @Override
    public List<Double> findTimeLine(Long meetingID) {
        return individualAMapper.findTimeLine(meetingID);
    }

    @Override
    public List<String> findUserList(Long meetingID) {
        return individualAMapper.findUsers(meetingID);
    }

    @Override
    public List<IndividualVO> findIndividualByUser(Long meetingID, String userName) {
        return individualAMapper.findByUser(meetingID,userName);
    }
}

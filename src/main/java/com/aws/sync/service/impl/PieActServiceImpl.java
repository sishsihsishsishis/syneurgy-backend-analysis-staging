package com.aws.sync.service.impl;

import com.aws.sync.entity.PieAct;
import com.aws.sync.entity.PieSpeaker;
import com.aws.sync.mapper.PieActMapper;
import com.aws.sync.mapper.PieSpeakerMapper;
import com.aws.sync.service.PieActService;
import com.aws.sync.service.PieSpeakerService;
import com.aws.sync.vo.PieActVO;
import com.aws.sync.vo.PieSpeakerVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PieActServiceImpl extends ServiceImpl<PieActMapper, PieAct> implements PieActService {
    @Resource
    PieActMapper pieActMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertPie(List<PieAct> pieActs) {
        return pieActMapper.addBatch(pieActs);
    }

    @Override
    public List<PieActVO> findAct(Long meetingID) {
        return pieActMapper.findAct(meetingID);
    }


}

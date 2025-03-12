package com.aws.sync.service;

import com.aws.sync.entity.PieAct;
import com.aws.sync.entity.PieSpeaker;
import com.aws.sync.vo.PieActVO;
import com.aws.sync.vo.PieSpeakerVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PieActService extends IService<PieAct> {
    int insertPie(List<PieAct> pieActs);

    List<PieActVO> findAct(Long meetingID);



}

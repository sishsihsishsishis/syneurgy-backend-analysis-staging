package com.aws.sync.mapper;


import com.aws.sync.entity.PieAct;
import com.aws.sync.entity.PieSpeaker;
import com.aws.sync.vo.PieActVO;
import com.aws.sync.vo.PieSpeakerVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PieActMapper extends BaseMapper<PieAct> {
    int addBatch(@Param("PieAct") List<PieAct> pieActs);

    List<PieActVO> findAct(Long meetingID);
}

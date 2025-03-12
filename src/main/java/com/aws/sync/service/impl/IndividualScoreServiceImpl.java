package com.aws.sync.service.impl;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.IndividualScore;
import com.aws.sync.entity.IndividualSyncR;
import com.aws.sync.mapper.IndividualRMapper;
import com.aws.sync.mapper.IndividualScoreMapper;
import com.aws.sync.service.IndividualRService;
import com.aws.sync.service.IndividualScoreService;
import com.aws.sync.vo.IndividualVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class IndividualScoreServiceImpl extends ServiceImpl<IndividualScoreMapper, IndividualScore> implements IndividualScoreService {
    @Autowired
    IndividualScoreMapper individualScoreMapper;

    @Override
    public RestResult getUserScore(Long meetingID) {
        LambdaQueryWrapper<IndividualScore> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(IndividualScore::getMeeting_id,meetingID);
        List<IndividualScore> individualScores = individualScoreMapper.selectList(lambdaQueryWrapper);
        HashMap<String,Object> ans = new HashMap<>();
        for (IndividualScore individualScore : individualScores) {
            String user = individualScore.getUsers();
            HashMap<String,Double> score = new HashMap<>();
            score.put("brain_score",null);
            score.put("body_score",individualScore.getBody_score());
            score.put("behavior_score",individualScore.getBehavior_score());
            ans.put(user,score);
        }
        return RestResult.success().data(ans);
    }
}

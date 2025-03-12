package com.aws.sync.service.impl;


import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.GroupEmoji;
import com.aws.sync.mapper.UniverseGroupEmojiMapper;
import com.aws.sync.service.UniverseGroupService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class UniverseGroupServiceImpl extends ServiceImpl<UniverseGroupEmojiMapper, GroupEmoji> implements UniverseGroupService {
    @Autowired
    UniverseGroupEmojiMapper universeGroupEmojiMapper;

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addGroupEmojiOneByOne(List<GroupEmoji> groupEmojiList) {
        SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH);
        UniverseGroupEmojiMapper geMapper = session.getMapper(UniverseGroupEmojiMapper.class);
        for (GroupEmoji groupEmoji : groupEmojiList) {
            geMapper.addUniverseGroupEmojiOneByOne(groupEmoji);
        }
        session.commit();
        session.close();
    }

    @Override
    public RestResult getUniverseGroupEmojiByMeetingId(Long meetingID) {
        LambdaQueryWrapper<GroupEmoji> universeGroupEmojiQueryWrapper = new LambdaQueryWrapper<>();
        universeGroupEmojiQueryWrapper.eq(GroupEmoji::getMeeting_id,meetingID)
                .orderByAsc(GroupEmoji::getTime_ms);
        List<GroupEmoji> groupEmojiList = universeGroupEmojiMapper.selectList(universeGroupEmojiQueryWrapper);
        List<Number> timeline = new ArrayList<>();
        List<Number> current = new ArrayList<>();
        List<Number> accAverage = new ArrayList<>();

        for (GroupEmoji groupEmoji : groupEmojiList) {
            timeline.add(groupEmoji.getTime_ms());
            current.add(groupEmoji.getCurrent());
            accAverage.add(groupEmoji.getAcc_average());
        }

        HashMap<String,List<Number>> ans = new HashMap<>();
        ans.put("timeline",timeline);
        ans.put("current",current);
        ans.put("acc_average",accAverage);
        return RestResult.success().data(ans);
    }
}

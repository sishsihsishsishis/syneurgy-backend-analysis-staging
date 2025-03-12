package com.aws.sync.service.impl;


import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.EmojiTable;
import com.aws.sync.entity.match.CVUser;
import com.aws.sync.mapper.EmojiMapper;
import com.aws.sync.service.CVUserService;
import com.aws.sync.service.EmojiService;
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
public class EmojiServiceImpl extends ServiceImpl<EmojiMapper, EmojiTable> implements EmojiService {
    @Autowired
    EmojiMapper emojiMapper;

    @Autowired
    CVUserService cvUserService;

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addEmojiOneByOne(List<EmojiTable> emojiTables) {
        SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH);
        EmojiMapper emMapper = session.getMapper(EmojiMapper.class);
        for (EmojiTable emojiTable : emojiTables) {
            emMapper.addEmojiOneByOne(emojiTable);
        }
        session.commit();
        session.close();
    }

    @Override
    public RestResult getEmojiByMeetingId(Long meetingID) {
        List<CVUser> userList = cvUserService.getUserList(meetingID);
        HashMap<String,Object> ans = new HashMap<>();
        List<Long> timeline = new ArrayList<>();
        for (int i = 0; i < userList.size(); i++) {
            List<Integer> emojiIds = new ArrayList<>();
            CVUser cvUser = userList.get(i);
            LambdaQueryWrapper<EmojiTable> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(EmojiTable::getMeeting_id,meetingID)
                    .eq(EmojiTable::getUsers,cvUser.getUser_name())
                    .orderByAsc(EmojiTable::getTime_ms);
            List<EmojiTable> emojiTables = emojiMapper.selectList(lambdaQueryWrapper);
            if(i == 0){
                for (EmojiTable emojiTable : emojiTables) {
                    timeline.add(emojiTable.getTime_ms());
                }
            }
            for (EmojiTable emojiTable : emojiTables) {
                emojiIds.add(emojiTable.getEmoji());
            }
            ans.put(cvUser.getUser_name(),emojiIds);
        }
        ans.put("timeline",timeline);
        return RestResult.success().data(ans);
    }
}

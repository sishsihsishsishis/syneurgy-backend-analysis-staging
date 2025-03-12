package com.aws.sync.service.impl;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.dto.UpdateSpeakerNameDTO;
import com.aws.sync.entity.match.SpeakerUser;
import com.aws.sync.mapper.SpeakerUserMapper;
import com.aws.sync.service.MeetingService;
import com.aws.sync.service.NlpService;
import com.aws.sync.service.SpeakerUserService;
import com.aws.sync.vo.SpeakerUserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpeakerUserServiceImpl extends ServiceImpl<SpeakerUserMapper, SpeakerUser> implements SpeakerUserService  {
    @Autowired
    SpeakerUserMapper speakerUserMapper;

    @Autowired
    MeetingService meetingService;

    @Override
    public RestResult updateSpeakerNames(UpdateSpeakerNameDTO updateSpeakerNameDTO) throws IOException {
//        for (UpdateSpeakerNameDTO.UserSpeaker userSpeaker : updateSpeakerNameDTO.getUser_speakers()) {
//            UpdateWrapper<SpeakerUser> updateWrapper = new UpdateWrapper<>();
//            updateWrapper.eq("meeting_id", updateSpeakerNameDTO.getMeeting_id())
//                    .eq("user_name", userSpeaker.getUser_name())
//                    .set("speaker_name", userSpeaker.getSpeaker_name());
//            speakerUserMapper.update(null, updateWrapper);
//        }
//        for (UpdateSpeakerNameDTO.UserSpeaker userSpeaker : updateSpeakerNameDTO.getUser_speakers()) {
//            List<String> speaker_name = userSpeaker.getSpeaker_name();
//            for (String speaker : speaker_name) {
//                save(new SpeakerUser(updateSpeakerNameDTO.getMeeting_id(),userSpeaker.getUser_name(),speaker));
//            }
//        }



//        boolean is_match = meetingService.checkMatch(updateSpeakerNameDTO.getMeeting_id());
//        if(is_match){
//            return RestResult.fail().data("已经匹配");
//        }
//
//        //清数据
//        if(meetingService.checkNlpHandle(updateSpeakerNameDTO.getMeeting_id())){
//            meetingService.removeNlpDataByMeetingId(updateSpeakerNameDTO.getMeeting_id());
//        }

        HashMap<String, List<String>> user_speakers = updateSpeakerNameDTO.getUser_speakers();
        for (Map.Entry<String, List<String>> stringListEntry : user_speakers.entrySet()) {
            String key = stringListEntry.getKey();
            List<String> value = stringListEntry.getValue();
            for (String s : value) {
                save(new SpeakerUser(updateSpeakerNameDTO.getMeeting_id(), key, s));
            }
        }

//        nlpService.handleNlp(updateSpeakerNameDTO.getMeeting_id());
//        meetingService.updateMatch(updateSpeakerNameDTO.getMeeting_id());
//        if(meetingService.checkCVHandle(updateSpeakerNameDTO.getMeeting_id())){
//            meetingService.updateHandle(updateSpeakerNameDTO.getMeeting_id());
//        }
        return RestResult.success().message("match success");
    }

    @Override
    public List<SpeakerUser> getSpeakerUsersByMeetingId(Long meeting_id) {
        QueryWrapper<SpeakerUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("meeting_id", meeting_id);
        return speakerUserMapper.selectList(queryWrapper);
    }

    @Override
    public List<String> getAllSpeakerNames(Long meeting_id) {
        LambdaQueryWrapper<SpeakerUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(SpeakerUser::getUser_name)
                .eq(SpeakerUser::getMeeting_id, meeting_id)
                .orderByAsc(SpeakerUser::getUser_name);
        List<SpeakerUser> speakerUsers = speakerUserMapper.selectList(lambdaQueryWrapper);
        List<String> speakerNames = new ArrayList<>();
        for(SpeakerUser s : speakerUsers){
            speakerNames.add(s.getUser_name().replace("user","speaker"));
        }
        return speakerNames;
    }

    @Override
    public RestResult getMatchUser(Long meetingID) {
        /*LambdaQueryWrapper<SpeakerUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SpeakerUser::getMeeting_id,meetingID);
        List<SpeakerUser> speakerUsers = speakerUserMapper.selectList(lambdaQueryWrapper);

        HashMap<String,List<String>> match = new HashMap<>();
        for (SpeakerUser speakerUser : speakerUsers) {
            match.computeIfAbsent(speakerUser.getUser_name(),k -> new ArrayList<>()).add(speakerUser.getSpeaker_name());
        }
        HashMap<String,Object> ans = new HashMap<>();
        ans.put("total_users",match.size());
        ans.put("match_result",match);
        return RestResult.success().data(ans);*/
        LambdaQueryWrapper<SpeakerUser> lambdaQueryWrapper = new LambdaQueryWrapper<SpeakerUser>()
                .eq(SpeakerUser::getMeeting_id, meetingID);
        List<SpeakerUser> speakerUsers = speakerUserMapper.selectList(lambdaQueryWrapper);
        return RestResult.success().data(speakerUsers);
    }

    @Override
    public HashMap<String,List<String>> getSpeakerMap(Long meetingID) {
        LambdaQueryWrapper<SpeakerUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SpeakerUser::getMeeting_id,meetingID);
        List<SpeakerUser> speakerUsers = speakerUserMapper.selectList(lambdaQueryWrapper);

        HashMap<String,List<String>> match = new HashMap<>();
        for (SpeakerUser speakerUser : speakerUsers) {
            match.computeIfAbsent(speakerUser.getUser_name(),k -> new ArrayList<>()).add(speakerUser.getSpeaker_name());
        }

        return match;
    }

    @Override
    public void deleteByMeetingID(Long meetingID) {
        Map<String, Object> map = new HashMap<>();
        map.put("meeting_id",meetingID);
        speakerUserMapper.deleteByMap(map);
    }

    @Override
    public SpeakerUser queryDataByMeetingIdAndName(Long meetingId, String username) {
        LambdaQueryWrapper<SpeakerUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SpeakerUser::getMeeting_id, meetingId)
                .eq(SpeakerUser::getUser_name, username);
        SpeakerUser entity = getOne(queryWrapper);
        return entity;
    }
}

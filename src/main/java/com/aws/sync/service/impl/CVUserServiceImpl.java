package com.aws.sync.service.impl;


import com.aws.sync.config.common.RestResult;
import com.aws.sync.dto.MatchDTO;
import com.aws.sync.entity.MeetingTable;
import com.aws.sync.entity.match.CVUser;
import com.aws.sync.mapper.CVUserMapper;
import com.aws.sync.service.CVUserService;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class CVUserServiceImpl extends ServiceImpl<CVUserMapper, CVUser> implements CVUserService {
    @Autowired
    CVUserMapper userMapper;

    @Override
    public List<CVUser> getUserList(Long meeting_id) {
        LambdaQueryWrapper<CVUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CVUser::getMeeting_id,meeting_id).orderByAsc(CVUser::getUser_name);
        List<CVUser> userList = userMapper.selectList(lambdaQueryWrapper);
        return userList;
    }

    @Override
    public int findUserCount(Long meetingID) {
        LambdaQueryWrapper<CVUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CVUser::getMeeting_id,meetingID);
        return Math.toIntExact(userMapper.selectCount(lambdaQueryWrapper));
    }

    @Override
    public RestResult matchLoginUser(Long meetingID, MatchDTO matchDTO) {
        //TODO:日志打印和异常处理
        int len = Math.min(matchDTO.getUsers().size(), matchDTO.getLoginUsers().size());
        for (int i = 0; i < len; i++) {
            UpdateWrapper<CVUser> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("meeting_id", meetingID)
                    .eq("user_name", matchDTO.getUsers().get(i));
            updateWrapper.set("is_match", 1);
            updateWrapper.set("user_id", matchDTO.getLoginUsers().get(i));

            userMapper.update(null, updateWrapper);
        }

        return RestResult.success().message("match success!");
    }

    @Override
    public RestResult getMatchResult(Long meetingID) {
        LambdaQueryWrapper<CVUser> lambdaQueryWrapper = new LambdaQueryWrapper<CVUser>()
                .eq(CVUser::getMeeting_id, meetingID);
        List<CVUser> cvUsers = userMapper.selectList(lambdaQueryWrapper);
        HashMap<String, Object> ans = new HashMap<>();
        ans.put("total_users", cvUsers.size());
        HashMap<String, String> match = new HashMap<>();
        for (CVUser cvUser : cvUsers) {
            match.put(cvUser.getUser_name(), cvUser.getUser_id());
        }
        ans.put("match_result", match);
        return RestResult.success().data(ans);
    }

    @Override
    public RestResult updateMatch(Long meetingID, String user) {
        return RestResult.success().message("success!");
    }

    @Override
    public RestResult updateEmailSend(Long meetingID, String user) {
        UpdateWrapper<CVUser> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("email_send",1);
        updateWrapper.eq("meeting_id",meetingID);
        updateWrapper.eq("user_name",user);
        userMapper.update(null,updateWrapper);
        return RestResult.success().message("success!");
    }

    @Override
    public List<CVUser> getByUserId(String userId) {
        LambdaQueryWrapper<CVUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CVUser::getUser_id, userId);
        return list(queryWrapper);
    }

    @Override
    public List<CVUser> getByUserIdAndTeamId(String userId, Long teamId) {
        LambdaQueryWrapper<CVUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CVUser::getUser_id, userId)
                .eq(CVUser::getTeam_id, teamId);
        return list(queryWrapper);
    }

    @Override
    public void handleUserMatch(String userId, Long username, Long meetingId) {
        LambdaUpdateWrapper<CVUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CVUser::getMeeting_id, meetingId)
                .eq(CVUser::getUser_name, username)
                .set(CVUser::getUser_id, userId)
                .set(CVUser::getIs_match, 1);
        update(null, updateWrapper);
    }

    @Override
    public List<CVUser> getMeetingAndUserMapByUserId(String userId) {
        LambdaQueryWrapper<CVUser> queryWrapper = new LambdaQueryWrapper<CVUser>()
                .eq(CVUser::getUser_id, userId)
                .orderByDesc(CVUser::getCreate_date);
        List<CVUser> cvUserList = list(queryWrapper);
//        HashMap<Long, String> map = new HashMap<>();
//        for (CVUser cvUser : cvUserList) {
//            map.put(cvUser.getMeeting_id(), cvUser.getUser_name());
//        }
//        return cvUserList.stream()
//                .collect(Collectors.toMap(CVUser::getMeeting_id, CVUser::getUser_name));
        return cvUserList;
    }
}

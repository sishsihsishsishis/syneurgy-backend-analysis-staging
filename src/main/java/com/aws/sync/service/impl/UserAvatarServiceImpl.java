package com.aws.sync.service.impl;

import com.aws.sync.advice.SystemException;
import com.aws.sync.config.common.ResultCodeEnum;
import com.aws.sync.entity.UserAvatar;
import com.aws.sync.mapper.UserAvatarMapper;
import com.aws.sync.service.UserAvatarService;
import com.aws.sync.vo.UserAvatarVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserAvatarServiceImpl extends ServiceImpl<UserAvatarMapper, UserAvatar> implements UserAvatarService {

    @Resource
    UserAvatarMapper userAvatarMapper;

    /**
     * 查询指定会议ID的所有用户头像信息。
     *
     * @param meetingID 需要查询头像的会议ID。
     * @return List<UserAvatarVO> 包含用户头像信息的列表。
     */
    @Override
    public List<UserAvatarVO> findUserAvatar(Long meetingID) {
        return userAvatarMapper.findAvatar(meetingID);
    }

    @Override
    public List<String> findUsers(Long meetingID) {
        return userAvatarMapper.findUsers(meetingID);
    }

    /**
     * 查询指定会议ID和用户名的用户头像URL。
     *
     * @param meetingID 会议的ID。
     * @param userName 用户名。
     * @return String 返回用户头像的URL。
     * @throws SystemException 如果没有找到对应的用户头像，则抛出异常。
     */
    @Override
    public String findUserAvatarUrl(Long meetingID, String userName) {
        LambdaQueryWrapper<UserAvatar> queryWrapper = new LambdaQueryWrapper<UserAvatar>()
                .eq(UserAvatar::getMeeting_id, meetingID)
                .eq(UserAvatar::getUsers, userName);
        UserAvatar userAvatar = userAvatarMapper.selectOne(queryWrapper);
        if (userAvatar == null) {
            throw new SystemException(ResultCodeEnum.ERR_AVATAR_NOT_FOUND);
        }
        return userAvatar.getUrl();
    }
}

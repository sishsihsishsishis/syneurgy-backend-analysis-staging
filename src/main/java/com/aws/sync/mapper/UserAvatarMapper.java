package com.aws.sync.mapper;

import com.aws.sync.entity.UserAvatar;
import com.aws.sync.vo.UserAvatarVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface UserAvatarMapper extends BaseMapper<UserAvatar> {

    List<UserAvatarVO> findAvatar(Long meetingID);

    List<String> findUsers(Long meetingID);
}

package com.aws.sync.service;


import com.aws.sync.entity.UserAvatar;
import com.aws.sync.vo.UserAvatarVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface UserAvatarService extends IService<UserAvatar> {
    List<UserAvatarVO> findUserAvatar(Long meetingID);

    List<String> findUsers(Long meetingID);

    String findUserAvatarUrl(Long meetingID, String userName);
}

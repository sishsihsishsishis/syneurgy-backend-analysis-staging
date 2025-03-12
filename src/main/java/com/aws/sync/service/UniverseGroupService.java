package com.aws.sync.service;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.EmojiTable;
import com.aws.sync.entity.GroupEmoji;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface UniverseGroupService extends IService<GroupEmoji> {

    void addGroupEmojiOneByOne(List<GroupEmoji> groupEmojiList);

    RestResult getUniverseGroupEmojiByMeetingId(Long meetingID);
}

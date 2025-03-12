package com.aws.sync.service;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.EmojiTable;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface EmojiService extends IService<EmojiTable> {

    void addEmojiOneByOne(List<EmojiTable> emojiTables);

    RestResult getEmojiByMeetingId(Long meetingID);
}

package com.aws.sync.mapper;

import com.aws.sync.entity.EmojiTable;
import com.aws.sync.entity.GroupEmoji;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UniverseGroupEmojiMapper extends BaseMapper<GroupEmoji> {
    void addUniverseGroupEmojiOneByOne(GroupEmoji groupEmoji);
}

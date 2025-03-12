package com.aws.sync.mapper;

import com.aws.sync.entity.EmojiTable;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface EmojiMapper extends BaseMapper<EmojiTable> {

    void addEmojiOneByOne(EmojiTable emojiTable);
}

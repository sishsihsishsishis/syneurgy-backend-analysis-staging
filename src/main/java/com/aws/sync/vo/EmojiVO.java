package com.aws.sync.vo;

import lombok.Data;

import javax.persistence.Column;

@Data
public class EmojiVO {
    private Integer emoji;
    private Long time_ms;
}

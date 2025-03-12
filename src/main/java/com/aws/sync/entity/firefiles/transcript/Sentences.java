package com.aws.sync.entity.firefiles.transcript;

import lombok.Data;

@Data
public class Sentences{
    private long index;
    private String text;
    private String raw_text;
    private long start_time;
    private long end_time;
    private long speaker_id;
    private String speaker_name;
}
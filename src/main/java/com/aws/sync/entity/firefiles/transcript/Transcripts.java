package com.aws.sync.entity.firefiles.transcript;

import com.aws.sync.entity.firefiles.User;
import lombok.Data;

import java.util.List;

@Data
public class Transcripts {
//    id title fireflies_users participants date transcript_url duration
    private String id;
    private String title;
    private List<String> fireflies_users;
    private List<String> participants;
    private long date;
    private String transcript_url;
    private long duration;

}




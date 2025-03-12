package com.aws.sync.entity.firefiles.transcript;

import com.aws.sync.entity.firefiles.User;
import com.aws.sync.entity.firefiles.transcript.Sentences;
import lombok.Data;
import java.util.List;

@Data
public class Transcript {
    private String id;
    private List<Sentences> sentences;
    private String title;
    private String host_email;
    private String organizer_email;
    private User user;
    private List<String> fireflies_users;
    private List<String> participants;
    private long date;
    private String transcript_url;
    private long duration;

}




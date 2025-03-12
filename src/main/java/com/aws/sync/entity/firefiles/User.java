package com.aws.sync.entity.firefiles;

import lombok.Data;

import java.util.List;

@Data
public class User {
        private String user_id;
        private String email;
        private String name;
        private long num_transcripts;
        private String recent_transcript;
        private long minutes_consumed;
        private boolean is_admin;
        private List<String> integrations;
    }
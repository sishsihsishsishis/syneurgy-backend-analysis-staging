package com.aws.sync.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class UpdateSpeakerNameDTO {
    private Long meeting_id;
    private HashMap<String,List<String>> user_speakers;
//    private List<UserSpeaker> user_speakers;
//
//    @Data
//    public static class UserSpeaker {
//        private String user_name;
//        private List<String> speaker_name;
//    }
}
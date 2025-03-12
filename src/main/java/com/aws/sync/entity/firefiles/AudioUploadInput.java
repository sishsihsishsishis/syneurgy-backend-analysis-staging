package com.aws.sync.entity.firefiles;

import lombok.Data;

import java.util.List;
@Data
public class AudioUploadInput {
    private String url;
    private String title;
    private List<Attendee> attendees;

    @Data
    public static class Attendee {
        private String displayName;
        private String email;
        private String phoneNumber;

    }
}

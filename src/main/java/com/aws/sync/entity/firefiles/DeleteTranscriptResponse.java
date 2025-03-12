package com.aws.sync.entity.firefiles;

import lombok.Data;

@Data
public class DeleteTranscriptResponse {
    private DeleteTranscriptData data;

    @Data
    public static class DeleteTranscriptData {
        private DeletedTranscript deleteTranscript;

    }
    @Data
    public static class DeletedTranscript {
        private String title;
        private String date;
        private String duration;
        private String organizer_email;

    }
}

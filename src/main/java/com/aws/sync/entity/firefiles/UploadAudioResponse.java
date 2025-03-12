package com.aws.sync.entity.firefiles;

import lombok.Data;

@Data
public class UploadAudioResponse {
    private UploadAudioData data;

    @Data
    public static class UploadAudioData {
        private AudioUpload uploadAudio;

    }

    @Data
    public static class AudioUpload {
        private boolean success;
        private String title;
        private String message;

    }
}

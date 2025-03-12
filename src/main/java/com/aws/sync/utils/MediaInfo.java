package com.aws.sync.utils;

import java.util.List;

public class MediaInfo {
    private String type; // "video" or "image"
    private String videoUrl;
    private String thumbnailUrl;
    private String copywriting;
    private List<String> imageUrls;

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getCopywriting() {
        return copywriting;
    }

    public void setCopywriting(String copywriting) {
        this.copywriting = copywriting;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    @Override
    public String toString() {
        return "MediaInfo{" +
                "type='" + type + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", copywriting='" + copywriting + '\'' +
                ", imageUrls=" + imageUrls +
                '}';
    }
}
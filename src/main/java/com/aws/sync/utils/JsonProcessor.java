package com.aws.sync.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class JsonProcessor {

    public static MediaInfo parseMediaInfo(String json) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(json);

        MediaInfo mediaInfo = new MediaInfo();
        JsonNode dataNode = rootNode.path("loaderData").path("video_(id)/page").path("videoInfoRes").path("item_list").get(0);

        if (dataNode.path("images").get(0) != null) {
            // It's an image gallery
            mediaInfo.setType("image");
            List<String> imageUrls = new ArrayList<>();
            dataNode.path("images").forEach(image -> imageUrls.add(image.path("url_list").get(0).asText()));
            mediaInfo.setImageUrls(imageUrls);
            mediaInfo.setCopywriting(dataNode.path("desc").asText());
        } else if (dataNode.has("video")) {
            // It's a video
            mediaInfo.setType("video");
            String video = dataNode.path("video").path("play_addr").path("uri").asText(); // 假设这是输入的 video 值
            String videoUrl;

            // 检查 video 是否包含 "mp3"
            if (video.contains("mp3")) {
                videoUrl = video;
            } else {
                videoUrl = "https://www.douyin.com/aweme/v1/play/?video_id=" + video;
            }

            System.out.println("Generated Video URL: " + videoUrl);
            mediaInfo.setVideoUrl(videoUrl);
            mediaInfo.setThumbnailUrl(dataNode.path("video").path("cover").path("url_list").get(0).asText());
            mediaInfo.setCopywriting(dataNode.path("desc").asText());
        }

        return mediaInfo;
    }
}

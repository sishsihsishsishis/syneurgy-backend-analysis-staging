package com.aws.sync.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

// MediaInfo class to encapsulate result


public class ShortLinkMediaParser {

    public static MediaInfo parseRed(String shortLink) throws IOException {
        MediaInfo mediaInfo = new MediaInfo();
        // 设置自定义 User-Agent
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36";

        // 使用 Jsoup 解析目标 URL
        String targetUrl = Jsoup.connect(shortLink)
                .followRedirects(true)
                .userAgent(userAgent)
                .execute()
                .url()
                .toString();
        // Follow redirects and fetch the final HTML content
//        String targetUrl = Jsoup.connect(shortLink).followRedirects(true).execute().url().toString();
        HttpURLConnection connection = (HttpURLConnection) new URL(targetUrl).openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        // Parse HTML content
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        Document document = Jsoup.parse(content.toString());

        // Check if it's a video
        Elements videoElements = document.select("meta[name=og:video]");
        if (!videoElements.isEmpty()) {
            mediaInfo.setType("video");
            mediaInfo.setVideoUrl(videoElements.attr("content"));

            // Thumbnail
            Elements thumbnailElements = document.select("meta[name=og:image]");
            if (!thumbnailElements.isEmpty()) {
                mediaInfo.setThumbnailUrl(thumbnailElements.attr("content"));
            }

            // Copywriting
            String title = document.select("meta[name=og:title]").attr("content");
            String description = document.select("meta[name=description]").attr("content");
            mediaInfo.setCopywriting((title.split(" ")[0].split("#")[0] + "\n" + description).trim());
        } else {
            // It's an image
            mediaInfo.setType("image");

            // Image URLs
            Elements imageElements = document.select("meta[name=og:image]");
            List<String> imageUrls = imageElements.stream()
                    .map(element -> element.attr("content"))
                    .collect(Collectors.toList());
            mediaInfo.setImageUrls(imageUrls);

            // Copywriting
            String title = document.select("meta[name=og:title]").attr("content");
            String description = document.select("meta[name=description]").attr("content");
            mediaInfo.setCopywriting((title.split(" ")[0].split("#")[0] + "\n" + description).trim());
        }

        return mediaInfo;
    }

    public static void main(String[] args) {
        String url = "http://xhslink.com/a/319Odr2hHNv1"; // Replace with your short link
//        String url = "http://xhslink.com/a/o3v111XnKNv1"; // Replace with your short link

        try {
            MediaInfo mediaInfo = parseRed(url);
            System.out.println(mediaInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
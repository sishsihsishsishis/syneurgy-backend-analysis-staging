//package com.aws.sync.service;
//
//import com.aws.sync.entity.ChatgptResponse;
//import io.github.flashvayne.chatgpt.dto.ChatRequest;
//import io.github.flashvayne.chatgpt.dto.ChatResponse;
//import io.github.flashvayne.chatgpt.dto.chat.MultiChatMessage;
//import io.github.flashvayne.chatgpt.dto.chat.MultiChatRequest;
//import io.github.flashvayne.chatgpt.dto.chat.MultiChatResponse;
//import io.github.flashvayne.chatgpt.dto.image.ImageFormat;
//import io.github.flashvayne.chatgpt.dto.image.ImageRequest;
//import io.github.flashvayne.chatgpt.dto.image.ImageResponse;
//import io.github.flashvayne.chatgpt.dto.image.ImageSize;
//import io.github.flashvayne.chatgpt.exception.ChatgptException;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Primary;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Slf4j
//@Service
//@Primary
//public class ChatgptServiceImpl implements ChatgptService {
//    private final String AUTHORIZATION = "Bearer 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";
//
//    private final RestTemplate restTemplate = new RestTemplate();
//    @Override
//    public String sendMessage(String message) {
//        return null;
//    }
//
//    @Override
//    public ChatgptResponse sendChatRequest(ChatgptResponse chatRequest) {
//        return this.getResponse(this.buildHttpEntity(chatRequest), ChatgptResponse.class, "https://api.openai.com/v1/chat/completions");
//    }
//
//    @Override
//    public String multiChat(List<MultiChatMessage> messages) {
//        return null;
//    }
//
//    @Override
//    public MultiChatResponse multiChatRequest(MultiChatRequest multiChatRequest) {
//        return null;
//    }
//
//    @Override
//    public String imageGenerate(String prompt) {
//        return null;
//    }
//
//    @Override
//    public List<String> imageGenerate(String prompt, Integer n, ImageSize size, ImageFormat format) {
//        return null;
//    }
//
//    @Override
//    public ImageResponse imageGenerateRequest(ImageRequest imageRequest) {
//        return null;
//    }
//
//    protected <T> HttpEntity<?> buildHttpEntity(T request) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
//        headers.add("Authorization", AUTHORIZATION);
//        return new HttpEntity<>(request, headers);
//    }
//
//    protected <T> T getResponse(HttpEntity<?> httpEntity, Class<T> responseType, String url) {
//        log.info("request url: {}, httpEntity: {}", url, httpEntity);
//        ResponseEntity<T> responseEntity = restTemplate.postForEntity(url, httpEntity, responseType);
//        if (responseEntity.getStatusCode().isError()) {
//            log.error("error response status: {}", responseEntity);
//            throw new ChatgptException("error response status :" + responseEntity.getStatusCode().value());
//        } else {
//            log.info("response: {}", responseEntity);
//        }
//        return responseEntity.getBody();
//    }
//}

//package com.aws.sync.gpt;
//
//import lombok.Data;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.BodyInserters;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//import java.util.List;
//
//@Service
//public class ChatGPTService {
//
//    private final WebClient webClient;
//
//    @Value("${openai.api.key}")
//    private String openAIApiKey;
//
//    public ChatGPTService() {
//        this.webClient = WebClient.builder()
//            .baseUrl("https://api.openai.com/v1")
//            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer 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")
//            .build();
//    }
//
//    public Mono<String> generateResponse(String input) {
//        return webClient.post()
//            .uri("/completions")
//            .contentType(MediaType.APPLICATION_JSON)
//            .body(BodyInserters.fromValue(createRequestBody(input)))
//            .retrieve()
//            .bodyToMono(ChatGPTResponse.class)
//            .map(ChatGPTResponse::getGeneratedResponse);
//    }
//
//    private ChatGPTRequest createRequestBody(String input) {
//        ChatGPTRequest requestBody = new ChatGPTRequest();
//        requestBody.setPrompt(input);
//        requestBody.setMaxTokens(50);
//        requestBody.setTemperature(0.7);
//        requestBody.setTopP(1);
//        requestBody.setFrequencyPenalty(0);
//        requestBody.setPresencePenalty(0);
//        requestBody.setModel("text-davinci-003");
//        return    requestBody;
//    }
//
//}
//
//@Data
//class ChatGPTRequest {
//    // 定义请求参数
//
//    private String prompt;
//    private int maxTokens;
//    private double temperature;
//    private double topP;
//    private int frequencyPenalty;
//    private int presencePenalty;
//    private String model;
//
//    // 生成 getter 和 setter 方法
//    // ...
//}
//
//@Data
//class ChatGPTResponse {
//    // 定义响应参数
//
//    private List<Choice> choices;
//
//    // 生成 getter 和 setter 方法
//    // ...
//
//    public String getGeneratedResponse() {
//        if (choices != null && !choices.isEmpty()) {
//            return choices.get(0).getText();
//        }
//        return null;
//    }
//}
//
//@Data
//class Choice {
//    private String text;
//
//    // 生成 getter 和 setter 方法
//    // ...
//}
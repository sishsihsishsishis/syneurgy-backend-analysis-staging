package com.aws.sync.controller;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.ResponseModel;
import com.aws.sync.entity.gpt.ChatCompletion;
import com.aws.sync.entity.gpt.Message;
import com.aws.sync.entity.gpt.RequestMessage;
import io.github.flashvayne.chatgpt.dto.ChatRequest;
import io.github.flashvayne.chatgpt.dto.ChatResponse;
import io.github.flashvayne.chatgpt.dto.Choice;
import io.github.flashvayne.chatgpt.service.ChatgptService;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
//import reactor.core.publisher.Mono;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@CrossOrigin
//@ApiOperation("chat")
public class ChatController {

//    private final ChatGPTService chatGPTService;

    @Autowired
    private ChatgptService chatgptService;

    @ApiOperation("chat")
    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        System.out.println("debug");
        ChatRequest chatRequest = new ChatRequest("text-davinci-003",message,2048,1.0,0.9);
        ChatResponse chatResponse = chatgptService.sendChatRequest(chatRequest);
        List<Choice> choices = chatResponse.getChoices();
        String text = choices.get(0).getText();
        System.out.println(text);
        return text;
    }

    @PostMapping("/chatCompletion")
    public ResponseEntity<ResponseModel> chatCompletion(@RequestBody String jsonRequestBody) {
        final String uri = "https://api.openai.com/v1/chat/completions";
        final String apiKey = "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";

        RestTemplate restTemplate = new RestTemplate();

        // Set headers
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + apiKey);

        // Create and send the request
        org.springframework.http.HttpEntity<String> request =
                new org.springframework.http.HttpEntity<>(jsonRequestBody, headers);
        ResponseEntity<ResponseModel> response = restTemplate.postForEntity(uri, request, ResponseModel.class);
        System.out.println("debug");
        return response;
    }


    @PostMapping("/getCompletion")
    public ResponseEntity<String> getCompletion(@RequestBody String jsonRequestBody) {
        final String uri = "https://api.openai.com/v1/completions";
        final String apiKey = "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";

        RestTemplate restTemplate = new RestTemplate();

        // Set headers
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + apiKey);

        // Create and send the request
        org.springframework.http.HttpEntity<String> request =
                new org.springframework.http.HttpEntity<>(jsonRequestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(uri, request, String.class);
        System.out.println("debug");
        System.out.println(response.getBody().toString());
        return response;
    }


    @PostMapping("/send")
    public RestResult sendMessageWithFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("message") String messageContent) {

        String originalFilename = file.getOriginalFilename();

        try {
            // 读取文件内容
            String fileContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            // 拼接文件内容和消息
            String finalMessage = fileContent + ".";

            System.out.println(finalMessage);
            final String uri = "https://api.openai.com/v1/chat/completions";
            final String apiKey = "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";

            RestTemplate restTemplate = new RestTemplate();

            // Set headers
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + apiKey);

//            Message m = new Message();
//            m.setPrompt(finalMessage);
            String systemPrompt = "Your identity:\n" +
                    "A highly skilled artificial intelligence trained in language comprehension and summarization.\n" +
                    "\n" +
                    "The text I will submit:\n" +
                    "I'm about to submit a lengthy text of the conversation. Each line represents a conversation record, the format is:\n" +
                    "<role>\\t<start time>\\t<end time>\\t<sentence>\\t<emotion>\\t<dialogue act>\n" +
                    "\n" +
                    "Where:\n" +
                    "<role> indicates the speaker's role.\n" +
                    "<begin time> is the start time of the sentence.\n" +
                    "<End time> is the end time of the sentence.\n" +
                    "<sentence> is the specific content of the sentence.\n" +
                    "<emotion> describes the emotion conveyed in the sentence.\n" +
                    "<Dialogue behavior> refers to the type or action of dialogue.\n" +
                    "\n" +
                    "Processing procedures:\n" +
                    "Submission method: I will submit the entire conversation text in one go.\n" +
                    "\n" +
                    "Paragraphs and summaries: Based on the context of the text, split the text into logical paragraphs. Paragraphs should revolve around a theme or idea, double-checking the data structure to make sure it is handled correctly. Summarize each paragraph and extract its core ideas or information. Ensure that the summary captures the topic and content of the passage very accurately. Based on the summary, try to merge multiple related summaries into a higher-level summary. Make sure there is no duplication of information between each paragraph and remove redundant details. Do not exceed five paragraphs. Then, Try to keep each summary to no more than 150 words.Finally, label each segment with a serial number.\n" +
                    "\n" +
                    "Time Range: Provides start and end timestamps for each paragraph. The timestamps before and after each paragraph are consecutive.\n" +
                    "\n" +
                    "Emphasis: Don’t show me the processing flow on the page, only show me the final result paragraph on the page.\n" +
                    "\n" +
                    "\n" +
                    "Example paragraph template for summary:\n" +
                    "1(0:00-300:00)The first topic of conversation in the weekly product marketing meeting is about training and cross mobility within marketing. The idea is to promote cross mobility by implementing a training curriculum. \n" +
                    "\n" +
                    "2(301:00-700:00) The second topic revolves around improving the solutions page on the website. The speaker discusses the current disorganized state of the solutions page and suggests rationalizing the content and categorizing it properly.\n";
            Message m = new Message("user", finalMessage);
            Message system = new Message("system", systemPrompt);
            List<Message> list = new ArrayList<>();
            list.add(system);
            list.add(m);
            RequestMessage r = new RequestMessage("gpt-3.5-turbo-16k", list);
            // Create and send the request
            org.springframework.http.HttpEntity<RequestMessage> request =
                    new org.springframework.http.HttpEntity<>(r, headers);

            ResponseEntity<ChatCompletion> response = restTemplate.postForEntity(uri, request, ChatCompletion.class);
            System.out.println("debug");
            ChatCompletion body = response.getBody();
            ChatCompletion.Choice choice = body.getChoices()[0];
            String content = choice.getMessage().getContent();
            System.out.println(content);
//            System.out.println(response.getBody().toString());
//            System.out.println(response.getBody().getChoices().get(0));
            return RestResult.success().data(content);
        } catch (IOException e) {
            return RestResult.fail();
        }
    }


//    @GetMapping("/img")
//    public String img(@RequestParam String message) {
//        System.out.println("debug");
//        String responseMessage = chatgptService.sendMessage(message);
//        System.out.print(responseMessage);
//        ChatRequest chatRequest = new ChatRequest("text-davinci-003",message,2048,1.0,0.9);
//        ChatResponse chatResponse = chatgptService.sendChatRequest(chatRequest);
//        List<Choice> choices = chatResponse.getChoices();
//        String text = choices.get(0).getText();
//        System.out.println(text);
//        return text;
//    }

    @ApiOperation("image")
    @GetMapping("/image")
    public String image( @RequestParam String prompt) {
        String imageUrl = chatgptService.imageGenerate(prompt);
        return imageUrl;
    }

//    @PostMapping("/chat")
//    public Mono<String> chat(@RequestParam String sessionId, @RequestBody String input) {
//        return chatGPTService.generateResponse(sessionId, input);
//    }


}

//@Data
//class Message {
//    private String model = "gpt-3.5-turbo-16k";
//    private String prompt;
//    private Integer max_tokens = 16000;
//    private Double temperature = 0.8;
////    {
////        "model": "gpt-3.5-turbo-instruct",
////            "prompt": "讲一个故事",
////            "max_tokens": 7,
////            "temperature": 0
////    }
//}







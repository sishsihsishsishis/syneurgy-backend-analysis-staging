package com.aws.sync.service.firefiles;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.firefiles.*;
import com.aws.sync.entity.firefiles.transcript.TranscriptResponse;
import com.aws.sync.entity.firefiles.transcript.TranscriptsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Scanner;

@Service
public class FireFilesService {

//    private static final String API_URL = "https://api.fireflies.ai/graphql/";
    @Value("${fireFiles.api-url}")
    private String API_URL;

    public RestResult getUserinfo(String token) {
        RestTemplate restTemplate = new RestTemplate();
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + token);

        // 设置请求体
        String query = "{ user { user_id email name num_transcripts recent_transcript minutes_consumed is_admin integrations } }";
        String jsonInputString = String.format("{ \"query\": \"%s\" }", query);

        // 创建 HttpEntity
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonInputString, headers);

        // 发送请求并接收返回值
        ResponseEntity<GraphQLResponse> response = restTemplate.exchange(API_URL, HttpMethod.POST, requestEntity, GraphQLResponse.class);

        // 返回解析后的对象
        return RestResult.success().data(response.getBody().getData());
    }

    public RestResult getUserinfoById(String token, String id) {
        RestTemplate restTemplate = new RestTemplate();
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + token);

        // 设置请求体
        String query = String.format("{ user(id:\\\"%s\\\") { user_id email name num_transcripts recent_transcript minutes_consumed is_admin integrations } }",id);
        String jsonInputString = String.format("{ \"query\": \"%s\" }", query);

        // 创建 HttpEntity
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonInputString, headers);

        // 发送请求并接收返回值
        ResponseEntity<GraphQLResponse> response = restTemplate.exchange(API_URL, HttpMethod.POST, requestEntity, GraphQLResponse.class);

        // 返回解析后的对象
        return RestResult.success().data(response.getBody().getData());
    }

    public RestResult getTranscriptById(String token, String transcriptId) {
        RestTemplate restTemplate = new RestTemplate();

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + token);

        // 设置请求体
        String requestBody = String.format("{ \"query\": \"{ transcript(id: \\\"%s\\\"){ id sentences{index text raw_text start_time end_time speaker_id speaker_name} title host_email organizer_email user {user_id email name num_transcripts recent_transcript recent_meeting minutes_consumed is_admin integrations} fireflies_users participants date transcript_url duration } }\" }", transcriptId);

        // 创建 HttpEntity
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        // 发送请求并接收返回值
        ResponseEntity<TranscriptResponse> response = restTemplate.exchange(API_URL, HttpMethod.POST, requestEntity, TranscriptResponse.class);

        // 返回解析后的对象
        return RestResult.success().data(response.getBody().getData().getTranscript());
    }

    public RestResult getTranscripts(String token) {
        RestTemplate restTemplate = new RestTemplate();

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + token);

        // 设置请求体
        String query = "{ transcripts  { id title fireflies_users participants date transcript_url duration } }";
        String jsonInputString = String.format("{ \"query\": \"%s\" }", query);

        // 创建 HttpEntity
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonInputString, headers);

        // 发送请求并接收返回值
        ResponseEntity<TranscriptsResponse> response = restTemplate.exchange(API_URL, HttpMethod.POST, requestEntity, TranscriptsResponse.class);

        // 返回解析后的对象
        return RestResult.success().data(response.getBody().getData().getTranscripts());
    }

    public RestResult uploadAudio(String token, AudioUploadInput input) {
        RestTemplate restTemplate = new RestTemplate();

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + token);

        // 将 input 对象序列化为 JSON 字符串
        ObjectMapper objectMapper = new ObjectMapper();
        String inputJson = "";
        try {
            inputJson = objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // 设置请求体
        String requestBody = String.format("{ \"query\": \"mutation($input: AudioUploadInput) { uploadAudio(input: $input) { success title message } }\", \"variables\": { \"input\": %s } }", inputJson);

        // 创建 HttpEntity
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        // 发送请求并接收返回值
        ResponseEntity<UploadAudioResponse> response = restTemplate.exchange(API_URL, HttpMethod.POST, requestEntity, UploadAudioResponse.class);

        // 返回解析后的对象
        return RestResult.success().data(response.getBody().getData());
    }

    public RestResult setUserRole(String token, String userId, String role) {
        RestTemplate restTemplate = new RestTemplate();

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + token);

        // 设置请求体
        String requestBody = String.format("{ \"query\": \"mutation($user_id: String!, $role: Role!) { setUserRole(user_id: $user_id, role:$role) { name is_admin } }\", \"variables\": { \"user_id\": \"%s\", \"role\": \"%s\" } }", userId, role);

        // 创建 HttpEntity
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        // 发送请求并接收返回值
        ResponseEntity<SetUserRoleResponse> response = restTemplate.exchange(API_URL, HttpMethod.POST, requestEntity, SetUserRoleResponse.class);

        // 返回解析后的对象
        return RestResult.success().data(response.getBody());
    }

    public RestResult deleteTranscript(String token, String transcriptId) {
        RestTemplate restTemplate = new RestTemplate();

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + token);

        // 设置请求体
        String requestBody = String.format("{ \"query\": \"mutation($transcriptId: String!) { deleteTranscript(id: $transcriptId) { title date duration organizer_email } }\", \"variables\": { \"transcriptId\": \"%s\" } }", transcriptId);

        // 创建 HttpEntity
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        // 发送请求并接收返回值
        ResponseEntity<DeleteTranscriptResponse> response = restTemplate.exchange(API_URL, HttpMethod.POST, requestEntity, DeleteTranscriptResponse.class);

        // 返回解析后的对象
        return RestResult.success().data(response.getBody());
    }
}

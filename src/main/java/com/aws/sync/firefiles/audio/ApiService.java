package com.aws.sync.firefiles.audio;

import com.aws.sync.entity.firefiles.AudioUploadInput;
import com.aws.sync.entity.firefiles.DeleteTranscriptResponse;
import com.aws.sync.entity.firefiles.UploadAudioResponse;
import com.aws.sync.entity.firefiles.SetUserRoleResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class ApiService {

    private static final String API_URL = "https://api.fireflies.ai/graphql/";


    public UploadAudioResponse uploadAudio(String token, AudioUploadInput input) {
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
        return response.getBody();
    }

    public SetUserRoleResponse setUserRole(String token, String userId, String role) {
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
        return response.getBody();
    }

    public DeleteTranscriptResponse deleteTranscript(String token, String transcriptId) {
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
        return response.getBody();
    }
}

//package com.aws.sync;
//
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.TestPropertySource;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestPropertySource(locations = "classpath:application-test.properties")
//public class MyTest {
//
//    @Autowired
//    RedisTemplate redisTemplate;
//
//    @Test
//    void TestRedisPut(){
//        redisTemplate.opsForZSet().add("meeting:video:63","test/video/meeting1.mp4",63);
//    }
//}
//
//// 定义音频上传参数
//class AudioUploadInput {
//    private String filename;
//    private String mimetype;
//    private byte[] content;
//
//    public AudioUploadInput(String filename, String mimetype, byte[] content) {
//        this.filename = filename;
//        this.mimetype = mimetype;
//        this.content = content;
//    }
//
//    public String getFilename() {
//        return filename;
//    }
//
//    public void setFilename(String filename) {
//        this.filename = filename;
//    }
//
//    public String getMimetype() {
//        return mimetype;
//    }
//
//    public void setMimetype(String mimetype) {
//        this.mimetype = mimetype;
//    }
//
//    public byte[] getContent() {
//        return content;
//    }
//
//    public void setContent(byte[] content) {
//        this.content = content;
//    }
//}

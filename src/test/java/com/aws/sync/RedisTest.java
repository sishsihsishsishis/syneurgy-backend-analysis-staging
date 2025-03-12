//package com.aws.sync;
//
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.RedisTemplate;
//
//import java.io.Serializable;
//
//@SpringBootTest
//public class RedisTest {
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @Test
//    void testPut(){
//        Video test = new Video("1234", "test");
//        redisTemplate.opsForHash().put("meetingId",test.getMeetingId(),test);
//    }
//
//
//
//    @Test
//    void testGet(){
//
//    }
//}
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//class Video implements Serializable{
//    private String meetingId;
//    private String url;
//}
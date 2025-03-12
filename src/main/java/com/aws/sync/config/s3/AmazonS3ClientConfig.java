package com.aws.sync.config.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
//public class AmazonS3ClientConfig {
//
//    @Value("${s3.storage.accessKeyId}")
//    private String accessKey;
//
//    @Value("${s3.storage.secretAccessKey}")
//    private String secretKey;
//
//    @Value("${s3.storage.region}")
//    private String region;
//
//    @Bean
//    public AmazonS3 amazonS3Client() {
//        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
//        return AmazonS3ClientBuilder.standard()
//                .withRegion(Regions.fromName(region))
//                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
//                .build();
//    }
//}
package com.aws.sync.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.regions.Region;

//@Configuration
//public class AwsConfiguration {
//
//	@Value("${cloud.aws.credentials.access-key}")
//	private String awsAccessKey;
//
//	@Value("${cloud.aws.credentials.secret-key}")
//	private String awsSecretKey;
//
//	@Primary
//	@Bean
//	public AmazonS3 amazonSQSAsync() {
//		return AmazonS3ClientBuilder.standard().withRegion(Regions.US_WEST_1)
//				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsAccessKey, awsSecretKey)))
//				.build();
//	}
//
//	@Bean
//	public AwsCredentialsProvider awsCredentialsProvider() {
//		return StaticCredentialsProvider.create(AwsBasicCredentials.create(awsAccessKey, awsSecretKey));
//	}
//
//	@Bean
//	public S3Client s3Client() {
//		return S3Client.builder()
//				.region(Region.of("us-west-1"))
//				.credentialsProvider(awsCredentialsProvider())
//				.build();
//	}
//}

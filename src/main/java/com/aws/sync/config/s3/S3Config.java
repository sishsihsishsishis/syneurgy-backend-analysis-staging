package com.aws.sync.config.s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    AmazonS3 s3 = null;

    @Value("${s3.storage.accessKeyId}")
    private String accessKeyId;

    @Value("${s3.storage.secretAccessKey}")
    private String accessKeySecret;

    @Value("${s3.storage.region}")
    private String regionName;

    @Value("${s3.storage.endpoint}")
    private String endpoint;

    @Bean
    public AmazonS3 s3() {
        ClientConfiguration config = new ClientConfiguration();
        AwsClientBuilder.EndpointConfiguration endpointConfig =
                new AwsClientBuilder.EndpointConfiguration(endpoint, regionName);
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKeyId, accessKeySecret);
        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);

        s3 = AmazonS3Client.builder()
                .withEndpointConfiguration(endpointConfig)
                .withClientConfiguration(config)
                .withCredentials(awsCredentialsProvider)
                .disableChunkedEncoding()
                .withPathStyleAccessEnabled(true)
                .build();
        return s3;
    }

    @Bean
    public TransferManager transferManager() {
        return TransferManagerBuilder.standard()
                .withS3Client(s3())
                .build();
    }
}

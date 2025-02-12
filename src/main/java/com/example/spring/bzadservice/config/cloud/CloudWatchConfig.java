package com.example.spring.bzadservice.config.cloud;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchevents.CloudWatchEventsClient;

@Configuration
public class CloudWatchConfig {

    @Value("${cloud.aws.credentials.access-key}")
    private String iamAccessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String iamSecretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public CloudWatchEventsClient cloudWatchEventsClient() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(iamAccessKey, iamSecretKey);

        return CloudWatchEventsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
package com.cloudflare.storage.config;

import com.cloudflare.storage.constant.S3DirectoryConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class S3ClientConfig {

    @Value("${cloudflare.account-id}")
    private String accountId;

    @Value("${cloudflare.access-key}")
    private String accessKey;

    @Value("${cloudflare.secret-key}")
    private String secretKey;
    
    @Bean
    public S3Client s3client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                accessKey,
                secretKey
        );

        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        return S3Client.builder()
                .endpointOverride(URI.create(String.format("https://%s.r2.cloudflarestorage.com", accountId)))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(S3DirectoryConstants.AUTO))
                .serviceConfiguration(serviceConfiguration)
                .build();
    }
}
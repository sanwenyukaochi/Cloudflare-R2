package com.cloudflare.storage.config;

import com.cloudflare.storage.constant.S3DirectoryConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.time.Duration;

@Configuration
public class S3AsyncClientConfig {

    @Value("${cloudflare.account-id}")
    private String accountId;

    @Value("${cloudflare.access-key}")
    private String accessKey;

    @Value("${cloudflare.secret-key}")
    private String secretKey;
    
    @Bean
    public S3AsyncClient s3AsyncClient() {

        SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder()
                .maxConcurrency(50)
                .connectionTimeout(Duration.ofSeconds(60))
                .readTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(60))
                .build();

        ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                .apiCallTimeout(Duration.ofMinutes(2))
                .apiCallAttemptTimeout(Duration.ofSeconds(90))
                .retryStrategy(RetryMode.STANDARD)
                .build();

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                accessKey,
                secretKey
        );

        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        return S3AsyncClient.builder()
                .endpointOverride(URI.create(String.format("https://%s.r2.cloudflarestorage.com", accountId)))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(S3DirectoryConstants.AUTO))
                .serviceConfiguration(serviceConfiguration)
                .httpClient(httpClient)
                .overrideConfiguration(overrideConfig)
                .build();
    }
}

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
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

import java.net.URI;
import java.time.Duration;

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

    @Bean
    public S3Presigner s3Presigner() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                accessKey,
                secretKey
        );

        return S3Presigner.builder()
                .endpointOverride(URI.create(String.format("https://%s.r2.cloudflarestorage.com", accountId)))
                .region(Region.of(S3DirectoryConstants.AUTO))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    @Bean
    public S3AsyncClient s3AsyncClient() {

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                accessKey,
                secretKey
        );

        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

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

        return S3AsyncClient.builder()
                .endpointOverride(URI.create(String.format("https://%s.r2.cloudflarestorage.com", accountId)))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(S3DirectoryConstants.AUTO))
                .serviceConfiguration(serviceConfiguration)
                .httpClient(httpClient)
                .overrideConfiguration(overrideConfig)
                .build();
    }

    @Bean
    public S3TransferManager s3TransferManager(S3AsyncClient s3AsyncClient) {
        return S3TransferManager.builder()
                .s3Client(s3AsyncClient)
                .build();
    }
}
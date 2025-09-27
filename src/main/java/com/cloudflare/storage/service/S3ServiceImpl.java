package com.cloudflare.storage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.waiters.S3AsyncWaiter;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl {
    private final S3AsyncClient asyncClient;

    /**
     * Asynchronously copies an object from one S3 bucket to another.
     *
     * @param sourceBucket The name of the source bucket
     * @param sourceObjectKey  The key (name) of the source object
     * @param targetBucket The name of the target bucket
     * @param targetObjectKey  The key (name) of the target object
     * @return a {@link CompletableFuture} that completes with the copy result as a {@link String}
     * @throws RuntimeException if the URL could not be encoded or an S3 exception occurred during the copy
     */
    public CompletableFuture<String> copyBucketObjectAsync(String sourceBucket, String sourceObjectKey, String targetBucket, String targetObjectKey) {
        CopyObjectRequest copyReq = CopyObjectRequest.builder()
                .sourceBucket(sourceBucket)
                .sourceKey(sourceObjectKey)
                .destinationBucket(targetBucket)
                .destinationKey(targetObjectKey)
                .build();

        CompletableFuture<CopyObjectResponse> response = asyncClient.copyObject(copyReq);
        response.whenComplete((copyRes, ex) -> {
            if (copyRes != null) {
                log.info("The {} was copied to {}", sourceObjectKey, targetBucket);
            } else {
                throw new RuntimeException("An S3 exception occurred during copy", ex);
            }
        });

        return response.thenApply(CopyObjectResponse::copyObjectResult)
                .thenApply(Object::toString);
    }


    /**
     * Deletes an S3 bucket asynchronously.
     *
     * @param bucket the name of the bucket to be deleted
     * @return a {@link CompletableFuture} that completes when the bucket deletion is successful, or throws a {@link RuntimeException}
     * if an error occurs during the deletion process
     */
    public CompletableFuture<Void> deleteBucketAsync(String bucket) {
        DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder()
                .bucket(bucket)
                .build();

        CompletableFuture<DeleteBucketResponse> response = asyncClient.deleteBucket(deleteBucketRequest);
        response.whenComplete((deleteRes, ex) -> {
            if (deleteRes != null) {
                log.info("{} was deleted.", bucket);
            } else {
                throw new RuntimeException("An S3 exception occurred during bucket deletion", ex);
            }
        });
        return response.thenApply(r -> null);
    }



    /**
     * Creates an S3 bucket asynchronously.
     *
     * @param bucketName the name of the S3 bucket to create
     * @return a {@link CompletableFuture} that completes when the bucket is created and ready
     * @throws RuntimeException if there is a failure while creating the bucket
     */
    public CompletableFuture<Void> createBucketAsync(String bucketName) {
        CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                .bucket(bucketName)
                .build();

        CompletableFuture<CreateBucketResponse> response = asyncClient.createBucket(bucketRequest);
        return response.thenCompose(resp -> {
            S3AsyncWaiter s3Waiter = asyncClient.waiter();
            HeadBucketRequest bucketRequestWait = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            CompletableFuture<WaiterResponse<HeadBucketResponse>> waiterResponseFuture =
                    s3Waiter.waitUntilBucketExists(bucketRequestWait);
            return waiterResponseFuture.thenAccept(waiterResponse -> {
                waiterResponse.matched().response().ifPresent(headBucketResponse -> {
                    log.info("{} is ready", bucketName);
                });
            });
        }).whenComplete((resp, ex) -> {
            if (ex != null) {
                throw new RuntimeException("Failed to create bucket", ex);
            }
        });
    }
}

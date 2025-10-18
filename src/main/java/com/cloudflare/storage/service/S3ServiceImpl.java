package com.cloudflare.storage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Publisher;
import software.amazon.awssdk.services.s3.waiters.S3AsyncWaiter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl {
    private final S3AsyncClient s3AsyncClient;

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

        CompletableFuture<CopyObjectResponse> response = s3AsyncClient.copyObject(copyReq);
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

        CompletableFuture<DeleteBucketResponse> response = s3AsyncClient.deleteBucket(deleteBucketRequest);
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

        CompletableFuture<CreateBucketResponse> response = s3AsyncClient.createBucket(bucketRequest);
        return response.thenCompose(resp -> {
            S3AsyncWaiter s3Waiter = s3AsyncClient.waiter();
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

    /**
     * Asynchronously retrieves the bytes of an object from an Amazon S3 bucket and writes them to a local file.
     *
     * @param bucketName the name of the S3 bucket containing the object
     * @param keyName    the key (or name) of the S3 object to retrieve
     * @param path       the local file path where the object's bytes will be written
     * @return a {@link CompletableFuture} that completes when the object bytes have been written to the local file
     */
    public CompletableFuture<Void> getObjectBytesAsync(String bucketName, String keyName, String path) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .key(keyName)
                .bucket(bucketName)
                .build();

        CompletableFuture<ResponseBytes<GetObjectResponse>> response = s3AsyncClient.getObject(objectRequest, AsyncResponseTransformer.toBytes());
        return response.thenAccept(objectBytes -> {
            try {
                byte[] data = objectBytes.asByteArray();
                Path filePath = Paths.get(path);
                Files.write(filePath, data);
                log.info("Successfully obtained bytes from an S3 object");
            } catch (IOException ex) {
                throw new RuntimeException("Failed to write data to file", ex);
            }
        }).whenComplete((resp, ex) -> {
            if (ex != null) {
                throw new RuntimeException("Failed to get object bytes from S3", ex);
            }
        });
    }

    /**
     * Asynchronously lists all objects in the specified S3 bucket.
     *
     * @param bucketName the name of the S3 bucket to list objects for
     * @return a {@link CompletableFuture} that completes when all objects have been listed
     */
    public CompletableFuture<Void> listAllObjectsAsync(String bucketName) {
        ListObjectsV2Request initialRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .maxKeys(1)
                .build();

        ListObjectsV2Publisher paginator = s3AsyncClient.listObjectsV2Paginator(initialRequest);
        return paginator.subscribe(response -> {
            response.contents().forEach(s3Object -> {
                log.info("Object key: {}", s3Object.key());
            });
        }).thenRun(() -> {
            log.info("Successfully listed all objects in the bucket: {}", bucketName);
        }).exceptionally(ex -> {
            throw new RuntimeException("Failed to list objects", ex);
        });
    }


    /**
     * Uploads a local file to an AWS S3 bucket asynchronously.
     *
     * @param bucketName the name of the S3 bucket to upload the file to
     * @param key        the key (object name) to use for the uploaded file
     * @param objectPath the local file path of the file to be uploaded
     * @return a {@link CompletableFuture} that completes with the {@link PutObjectResponse} when the upload is successful, or throws a {@link RuntimeException} if the upload fails
     */
    public CompletableFuture<PutObjectResponse> uploadLocalFileAsync(String bucketName, String key, String objectPath) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        CompletableFuture<PutObjectResponse> response = s3AsyncClient.putObject(objectRequest, AsyncRequestBody.fromFile(Paths.get(objectPath)));
        return response.whenComplete((resp, ex) -> {
            if (ex != null) {
                throw new RuntimeException("Failed to upload file", ex);
            }
        });
    }

    /**
     * Uploads a file to an S3 bucket using the S3AsyncClient and enabling multipart support.
     *
     * @param filePath the local file path of the file to be uploaded
     */
    public CompletableFuture<PutObjectResponse> multipartUploadWithS3AsyncClient(String bucketName, String key, String filePath) {
        // Enable multipart support.
        S3AsyncClient s3AsyncClient = S3AsyncClient.builder()
                .multipartEnabled(true)
                .build();

        CompletableFuture<PutObjectResponse> response = s3AsyncClient.putObject(b -> b
                        .bucket(bucketName)
                        .key(key),
                Paths.get(filePath));

        response.join();
        log.info("File uploaded in multiple 8 MiB parts using S3AsyncClient.");
        return response.whenComplete((resp, ex) -> {
            if (ex != null) {
                throw new RuntimeException("Failed to upload file", ex);
            }
        });
    }

    /**
     * @param bucketName - The name of the bucket.
     * @param key - The name of the object.
     * @return software.amazon.awssdk.services.s3.model.PutObjectResponse - Returns metadata pertaining to the put object operation.
     */
    public PutObjectResponse putObjectFromStreamCrt(String bucketName, String key) {

        // AsyncExampleUtils.randomString() returns a random string up to 100 characters.
        String randomString = AsyncExampleUtils.randomString();
        log.info("random string to upload: {}: length={}", randomString, randomString.length());
        InputStream inputStream = new ByteArrayInputStream(randomString.getBytes());

        // Executor required to handle reading from the InputStream on a separate thread so the main upload is not blocked.
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // Specify `null` for the content length when you don't know the content length.
        AsyncRequestBody body = AsyncRequestBody.fromInputStream(inputStream, null, executor);

        CompletableFuture<PutObjectResponse> responseFuture =
                s3AsyncClient.putObject(r -> r.bucket(bucketName).key(key), body);

        PutObjectResponse response = responseFuture.join(); // Wait for the response.
        log.info("Object {} uploaded to bucket {}.", key, bucketName);
        executor.shutdown();
        return response;
    }


}

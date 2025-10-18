package com.cloudflare.storage.service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public interface S3DirectoryService {
    boolean abortDirectoryBucketMultipartUpload(String bucketName, String objectKey, String uploadId);
    boolean completeDirectoryBucketMultipartUpload(String bucketName, String objectKey, String uploadId, List<CompletedPart> uploadParts);
    void copyDirectoryBucketObject(String sourceBucket, String sourceObjectKey, String targetBucket, String targetObjectKey);
    void createDirectoryBucket(String bucketName, String zone) throws S3Exception;
    String createDirectoryBucketMultipartUpload(String bucketName, String objectKey);
    void deleteDirectoryBucket(String bucketName);
    void deleteDirectoryBucketEncryption(String bucketName);
    void deleteDirectoryBucketPolicy(String bucketName);
    void deleteDirectoryBucketObject(String bucketName, String objectKey);
    void deleteDirectoryBucketObjects(String bucketName, List<String> objectKeys);
    String getDirectoryBucketEncryption(String bucketName);
    String getDirectoryBucketPolicy(String bucketName);
    boolean getDirectoryBucketObject(String bucketName, String objectKey);
    boolean getDirectoryBucketObjectAttributes(String bucketName, String objectKey);
    boolean headDirectoryBucket(String bucketName);
    boolean headDirectoryBucketObject(String bucketName, String objectKey);
    List<String> listDirectoryBuckets();
    List<MultipartUpload> listDirectoryBucketMultipartUploads(String bucketName);
    List<String> listDirectoryBucketObjectsV2(String bucketName);
    List<Part> listDirectoryBucketMultipartUploadParts(String bucketName, String objectKey, String uploadId);
    void putDirectoryBucketEncryption(String bucketName, String kmsKeyId);
    void putDirectoryBucketPolicy(String bucketName, String policyText);
    void putDirectoryBucketObject(String bucketName, String objectKey, Path filePath);
    List<CompletedPart> multipartUploadForDirectoryBucket(String bucketName, String objectKey, String uploadId, Path filePath) throws IOException;
    List<CompletedPart> multipartUploadCopyForDirectoryBucket(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey, String uploadId);
    String getObjectUrlForDirectoryBucket(String bucketName, String keyName);
    void listTags(String bucketName, String keyName);
    void getObjectBytes(String bucketName, String keyName, String path);
    ObjectLockLegalHold getObjectLegalHold(String bucketName, String objectKey);
    void getBucketObjectLockConfiguration(String bucketName);
    ObjectLockRetention getObjectRetention(String bucketName, String key);
    void listBucketObjects(String bucketName);
    void setBucketAcl(String bucketName, String id);
    void deleteBucketCorsInformation(String bucketName, String accountId);
    void getBucketCorsInformation(String bucketName, String accountId);
    void setCorsInformation(String bucketName, String accountId);
    void setLifecycleConfig(String bucketName, String accountId);
    void getLifecycleConfig(String bucketName, String accountId);
    void deleteLifecycleConfig(String bucketName, String accountId);

    void setPolicy(String bucketName, String policyText);

    String getBucketPolicyFromFile(String policyFile);

    void multipartUploadWithS3Client(String bucketName, String key, String filePath);
}

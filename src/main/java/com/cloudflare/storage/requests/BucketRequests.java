package com.cloudflare.storage.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.nio.file.Path;
import java.util.List;

@NoArgsConstructor
public final class BucketRequests {
    public record AbortMultipartUploadRequest(String bucketName, String objectKey, String uploadId) {}
    public record CompleteMultipartUploadRequest(String bucketName, String objectKey, String uploadId, List<CompletedPart> uploadParts) {}
    public record CopyObjectRequest(String sourceBucket, String objectKey, String targetBucket) {}
    public record CreateBucketRequest(String bucketName, @JsonProperty(defaultValue = "auto") String zone) {}
    public record CreateMultipartUploadRequest(String bucketName, String objectKey) {}
    public record DeleteBucketRequest(String bucketName) {}
    public record DeleteBucketEncryptionRequest(String bucketName) {}
    public record DeleteBucketPolicyRequest(String bucketName) {}
    public record DeleteObjectRequest(String bucketName, String objectKey) {}
    public record DeleteObjectsRequest(String bucketName, List<String> objectKeys) {}
    public record GetBucketEncryptionRequest(String bucketName) {}
    public record GetBucketPolicyRequest(String bucketName) {}
    public record GetObjectRequest(String bucketName, String objectKey) {}
    public record GetObjectAttributesRequest(String bucketName, String objectKey) {}
    public record HeadBucketRequest(String bucketName) {}
    public record HeadObjectRequest(String bucketName, String objectKey) {}
    
    public record ListMultipartUploadsRequest(String bucketName) {}
    public record ListObjectsV2Request(String bucketName) {}
    public record ListMultipartUploadPartsRequest(String bucketName, String objectKey, String uploadId) {}
    public record PutBucketEncryptionRequest(String bucketName, String kmsKeyId) {}
    public record PutBucketPolicyRequest(String bucketName, String policyText) {}
    public record PutObjectRequest(String bucketName, String objectKey, Path filePath) {}
    public record MultipartUploadRequest(String bucketName, String objectKey, String uploadId, Path filePath) {}
    public record MultipartUploadCopyRequest(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey, String uploadId) {}
}

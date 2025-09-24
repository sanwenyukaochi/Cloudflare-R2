package com.cloudflare.storage.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.nio.file.Path;
import java.util.List;

@NoArgsConstructor
public final class BucketRequests {
    public record AbortMultipartUploadRequest(@NotBlank String bucketName, @NotBlank String objectKey, @NotBlank @NotNull @Size(min = 1)String uploadId) {}
    public record CompleteMultipartUploadRequest(@NotBlank String bucketName, @NotBlank String objectKey, @NotBlank String uploadId, List<CompletedPart> uploadParts) {}
    public record CopyObjectRequest(@NotBlank String sourceBucket, @NotBlank String sourceObjectKey, @NotBlank String targetBucket, @NotBlank String targetObjectKey) {}
    public record CreateBucketRequest(@NotBlank String bucketName, @JsonProperty(defaultValue = "auto") String zone) {}
    public record CreateMultipartUploadRequest(@NotBlank String bucketName, @NotBlank String objectKey) {}
    public record DeleteBucketRequest(@NotBlank String bucketName) {}
    public record DeleteBucketEncryptionRequest(@NotBlank String bucketName) {}
    public record DeleteBucketPolicyRequest(@NotBlank String bucketName) {}
    public record DeleteObjectRequest(@NotBlank String bucketName, @NotBlank String objectKey) {}
    public record DeleteObjectsRequest(@NotBlank String bucketName, @NotNull @Size(min = 1) List<String> objectKeys) {}
    public record GetBucketEncryptionRequest(@NotBlank String bucketName) {}
    public record GetBucketPolicyRequest(@NotBlank String bucketName) {}
    public record GetObjectRequest(@NotBlank String bucketName, @NotBlank String objectKey) {}
    public record GetObjectAttributesRequest(@NotBlank String bucketName, @NotBlank String objectKey) {}
    public record HeadBucketRequest(@NotBlank String bucketName) {}
    public record HeadObjectRequest(@NotBlank String bucketName, @NotBlank String objectKey) {}
    
    public record ListMultipartUploadsRequest(@NotBlank String bucketName) {}
    public record ListObjectsV2Request(@NotBlank String bucketName) {}
    public record ListMultipartUploadPartsRequest(@NotBlank String bucketName, @NotBlank String objectKey, @NotBlank String uploadId) {}
    public record PutBucketEncryptionRequest(@NotBlank String bucketName, @NotBlank String kmsKeyId) {}
    public record PutBucketPolicyRequest(@NotBlank String bucketName, @NotBlank String policyText) {}
    public record PutObjectRequest(@NotBlank String bucketName, @NotBlank String objectKey, @NotNull Path filePath) {}
    public record MultipartUploadRequest(@NotBlank String bucketName, @NotBlank String objectKey, @NotBlank String uploadId, @NotNull Path filePath) {}
    public record MultipartUploadCopyRequest(@NotBlank String sourceBucket, @NotBlank String sourceKey, @NotBlank String destinationBucket, @NotBlank String destinationKey, @NotBlank String uploadId) {}
}

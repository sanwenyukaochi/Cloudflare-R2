package com.playground.java.controller;


import com.playground.java.requests.BucketRequests.*;
import com.playground.java.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import software.amazon.awssdk.services.s3.model.MultipartUpload;
import software.amazon.awssdk.services.s3.model.Part;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/s3")
public class S3Controller {
    private final S3Service s3Service;

    @PostMapping("/abortDirectoryBucketMultipartUpload")
    public boolean abortDirectoryBucketMultipartUpload(@RequestBody AbortMultipartUploadRequest req) {
        return s3Service.abortDirectoryBucketMultipartUpload(req.bucketName(), req.objectKey(), req.uploadId());
    }

    @PostMapping("/completeDirectoryBucketMultipartUpload")
    public boolean completeDirectoryBucketMultipartUpload(@RequestBody CompleteMultipartUploadRequest req) {
        return s3Service.completeDirectoryBucketMultipartUpload(req.bucketName(), req.objectKey(), req.uploadId(), req.uploadParts());
    }

    @PostMapping("/copyDirectoryBucketObject")
    public void copyDirectoryBucketObject(@RequestBody CopyObjectRequest req) {
        s3Service.copyDirectoryBucketObject(req.sourceBucket(), req.objectKey(), req.targetBucket());
    }

    @PostMapping("/createDirectoryBucket")
    public void createDirectoryBucket(@RequestBody CreateBucketRequest req) {
        s3Service.createDirectoryBucket(req.bucketName(), req.zone());
    }

    @PostMapping("/createDirectoryBucketMultipartUpload")
    public String createDirectoryBucketMultipartUpload(@RequestBody CreateMultipartUploadRequest req) {
        return s3Service.createDirectoryBucketMultipartUpload(req.bucketName(), req.objectKey());
    }


    @DeleteMapping("/deleteDirectoryBucket")
    public void deleteDirectoryBucket(@RequestBody DeleteBucketRequest req) {
        s3Service.deleteDirectoryBucket(req.bucketName());
    }

    @DeleteMapping("/deleteDirectoryBucketEncryption")
    public void deleteDirectoryBucketEncryption(@RequestBody DeleteBucketEncryptionRequest req) {
        s3Service.deleteDirectoryBucketEncryption(req.bucketName());
    }

    @DeleteMapping("/deleteDirectoryBucketPolicy")
    public void deleteDirectoryBucketPolicy(@RequestBody DeleteBucketPolicyRequest req) {
        s3Service.deleteDirectoryBucketPolicy(req.bucketName());
    }

    @DeleteMapping("/deleteDirectoryBucketObject")
    public void deleteDirectoryBucketObject(@RequestBody DeleteObjectRequest req) {
        s3Service.deleteDirectoryBucketObject(req.bucketName(), req.objectKey());
    }

    @DeleteMapping("/deleteDirectoryBucketObjects")
    public void deleteDirectoryBucketObjects(@RequestBody DeleteObjectsRequest req) {
        s3Service.deleteDirectoryBucketObjects(req.bucketName(), req.objectKeys());
    }


    @GetMapping("/getDirectoryBucketEncryption")
    public String getDirectoryBucketEncryption(@RequestBody GetBucketEncryptionRequest req) {
        return s3Service.getDirectoryBucketEncryption(req.bucketName());
    }


    @GetMapping("/getDirectoryBucketPolicy")
    public String getDirectoryBucketPolicy(@RequestBody GetBucketPolicyRequest req) {
        return s3Service.getDirectoryBucketPolicy(req.bucketName());
    }


    @GetMapping("/getDirectoryBucketObject")
    public boolean getDirectoryBucketObject(@RequestBody GetObjectRequest req) {
        return s3Service.getDirectoryBucketObject(req.bucketName(), req.objectKey());
    }

    @GetMapping("/getDirectoryBucketObjectAttributes")
    public boolean getDirectoryBucketObjectAttributes(@RequestBody GetObjectAttributesRequest req) {
        return s3Service.getDirectoryBucketObjectAttributes(req.bucketName(), req.objectKey());
    }


    @PostMapping("/headDirectoryBucket")
    public boolean headDirectoryBucket(@RequestBody HeadBucketRequest req) {
        return s3Service.headDirectoryBucket(req.bucketName());
    }

    @PostMapping("/headDirectoryBucketObject")
    public boolean headDirectoryBucketObject(@RequestBody HeadObjectRequest req) {
        return s3Service.headDirectoryBucketObject(req.bucketName(), req.objectKey());
    }

    @GetMapping("/listDirectoryBuckets")
    public List<String> listDirectoryBuckets() {
        return s3Service.listDirectoryBuckets();
    }


    @GetMapping("/listDirectoryBucketMultipartUploads")
    public List<MultipartUpload> listDirectoryBucketMultipartUploads(@RequestBody ListMultipartUploadsRequest req) {
        return s3Service.listDirectoryBucketMultipartUploads(req.bucketName());
    }


    @GetMapping("/listDirectoryBucketObjectsV2")
    public List<String> listDirectoryBucketObjectsV2(@RequestBody ListObjectsV2Request req) {
        return s3Service.listDirectoryBucketObjectsV2(req.bucketName());
    }


    @PostMapping("/listDirectoryBucketMultipartUploadParts")
    public List<Part> listDirectoryBucketMultipartUploadParts(@RequestBody ListMultipartUploadPartsRequest req) {
        return s3Service.listDirectoryBucketMultipartUploadParts(req.bucketName(), req.objectKey(), req.uploadId());
    }


    @PutMapping("/putDirectoryBucketEncryption")
    public void putDirectoryBucketEncryption(@RequestBody PutBucketEncryptionRequest req) {
        s3Service.putDirectoryBucketEncryption(req.bucketName(), req.kmsKeyId());
    }

    @PutMapping("/putDirectoryBucketPolicy")
    public void putDirectoryBucketPolicy(@RequestBody PutBucketPolicyRequest req) {
        s3Service.putDirectoryBucketPolicy(req.bucketName(), req.policyText());
    }

    @PutMapping("/putDirectoryBucketObject")
    public void putDirectoryBucketObject(@RequestBody PutObjectRequest req) {
        s3Service.putDirectoryBucketObject(req.bucketName(), req.objectKey(), req.filePath());
    }

    @PostMapping("/multipartUploadForDirectoryBucket")
    public List<CompletedPart> multipartUploadForDirectoryBucket(@RequestBody MultipartUploadRequest req) throws IOException {
        Path filePath = req.filePath();
        return s3Service.multipartUploadForDirectoryBucket(req.bucketName(), req.objectKey(), req.uploadId(), filePath);
    }

    @PostMapping("/multipartUploadCopyForDirectoryBucket")
    public List<CompletedPart> multipartUploadCopyForDirectoryBucket(@RequestBody MultipartUploadCopyRequest req) {
        return s3Service.multipartUploadCopyForDirectoryBucket(
                req.sourceBucket(), req.sourceKey(), req.destinationBucket(), req.destinationKey(), req.uploadId()
        );
    }
}

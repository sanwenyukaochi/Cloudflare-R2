package com.cloudflare.storage.controller;

import com.cloudflare.storage.requests.S3DirectoryRequests.*;
import com.cloudflare.storage.service.S3DirectoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Cloudflare R2 Bucket API", description = "Cloudflare R2 目录桶操作接口")
public class S3DirectoryController {
    private final S3DirectoryService s3DirectoryService;

    @Operation(summary = "复制对象", description = "从源桶复制对象到目标桶")
    @PostMapping("/copyDirectoryBucketObject")
    public void copyDirectoryBucketObject(@Valid @RequestBody CopyObjectRequest req) {
        s3DirectoryService.copyDirectoryBucketObject(req.sourceBucket(), req.sourceObjectKey(), req.targetBucket(), req.targetObjectKey());
    }

    @Operation(summary = "创建目录桶", description = "创建一个新的目录桶，可指定可用区")
    @PostMapping("/createDirectoryBucket")
    public void createDirectoryBucket(@Valid @RequestBody CreateBucketRequest req) {
        s3DirectoryService.createDirectoryBucket(req.bucketName(), req.zone());
    }

    @Operation(summary = "删除目录桶", description = "删除指定的目录桶（桶必须为空）")
    @DeleteMapping("/deleteDirectoryBucket")
    public void deleteDirectoryBucket(@Valid @RequestBody DeleteBucketRequest req) {
        s3DirectoryService.deleteDirectoryBucket(req.bucketName());
    }

    @Operation(summary = "删除桶加密配置", description = "删除目录桶的加密配置")
    @DeleteMapping("/deleteDirectoryBucketEncryption")
    public void deleteDirectoryBucketEncryption(@Valid @RequestBody DeleteBucketEncryptionRequest req) {
        s3DirectoryService.deleteDirectoryBucketEncryption(req.bucketName());
    }

    @Operation(summary = "删除桶策略", description = "删除目录桶的访问策略")
    @DeleteMapping("/deleteDirectoryBucketPolicy")
    public void deleteDirectoryBucketPolicy(@Valid @RequestBody DeleteBucketPolicyRequest req) {
        s3DirectoryService.deleteDirectoryBucketPolicy(req.bucketName());
    }

    @Operation(summary = "删除对象", description = "删除目录桶中的单个对象")
    @DeleteMapping("/deleteDirectoryBucketObject")
    public void deleteDirectoryBucketObject(@Valid @RequestBody DeleteObjectRequest req) {
        s3DirectoryService.deleteDirectoryBucketObject(req.bucketName(), req.objectKey());
    }

    @Operation(summary = "批量删除对象", description = "批量删除目录桶中的多个对象")
    @DeleteMapping("/deleteDirectoryBucketObjects")
    public void deleteDirectoryBucketObjects(@Valid @RequestBody DeleteObjectsRequest req) {
        s3DirectoryService.deleteDirectoryBucketObjects(req.bucketName(), req.objectKeys());
    }

    @Operation(summary = "获取桶加密配置", description = "获取目录桶的加密配置信息")
    @GetMapping("/getDirectoryBucketEncryption")
    public String getDirectoryBucketEncryption(@Valid @RequestBody GetBucketEncryptionRequest req) {
        return s3DirectoryService.getDirectoryBucketEncryption(req.bucketName());
    }

    @Operation(summary = "获取桶策略", description = "获取目录桶的访问策略")
    @GetMapping("/getDirectoryBucketPolicy")
    public String getDirectoryBucketPolicy(@Valid @RequestBody GetBucketPolicyRequest req) {
        return s3DirectoryService.getDirectoryBucketPolicy(req.bucketName());
    }

    @Operation(summary = "获取对象", description = "从目录桶中获取指定对象")
    @GetMapping("/getDirectoryBucketObject")
    public boolean getDirectoryBucketObject(@Valid @RequestBody GetObjectRequest req) {
        return s3DirectoryService.getDirectoryBucketObject(req.bucketName(), req.objectKey());
    }

    @Operation(summary = "获取对象属性", description = "获取对象的元数据信息，如大小、ETag等")
    @GetMapping("/getDirectoryBucketObjectAttributes")
    public boolean getDirectoryBucketObjectAttributes(@Valid @RequestBody GetObjectAttributesRequest req) {
        return s3DirectoryService.getDirectoryBucketObjectAttributes(req.bucketName(), req.objectKey());
    }

    @Operation(summary = "检查桶是否存在", description = "检查指定的目录桶是否存在")
    @PostMapping("/headDirectoryBucket")
    public boolean headDirectoryBucket(@Valid @RequestBody HeadBucketRequest req) {
        return s3DirectoryService.headDirectoryBucket(req.bucketName());
    }

    @Operation(summary = "检查对象是否存在", description = "检查目录桶中指定对象是否存在")
    @PostMapping("/headDirectoryBucketObject")
    public boolean headDirectoryBucketObject(@Valid @RequestBody HeadObjectRequest req) {
        return s3DirectoryService.headDirectoryBucketObject(req.bucketName(), req.objectKey());
    }

    @Operation(summary = "列出所有目录桶", description = "获取当前账号下所有目录桶的名称列表")
    @GetMapping("/listDirectoryBuckets")
    public List<String> listDirectoryBuckets() {
        return s3DirectoryService.listDirectoryBuckets();
    }

    @Operation(summary = "列出桶中对象", description = "列出目录桶中的所有对象")
    @GetMapping("/listDirectoryBucketObjectsV2")
    public List<String> listDirectoryBucketObjectsV2(@Valid @RequestBody ListObjectsV2Request req) {
        return s3DirectoryService.listDirectoryBucketObjectsV2(req.bucketName());
    }

    @Operation(summary = "设置桶加密", description = "为目录桶设置加密配置")
    @PutMapping("/putDirectoryBucketEncryption")
    public void putDirectoryBucketEncryption(@Valid @RequestBody PutBucketEncryptionRequest req) {
        s3DirectoryService.putDirectoryBucketEncryption(req.bucketName(), req.kmsKeyId());
    }

    @Operation(summary = "设置桶策略", description = "为目录桶设置访问策略")
    @PutMapping("/putDirectoryBucketPolicy")
    public void putDirectoryBucketPolicy(@Valid @RequestBody PutBucketPolicyRequest req) {
        s3DirectoryService.putDirectoryBucketPolicy(req.bucketName(), req.policyText());
    }

    @Operation(summary = "上传对象", description = "将本地文件上传到目录桶中")
    @PutMapping("/putDirectoryBucketObject")
    public void putDirectoryBucketObject(@Valid @RequestBody PutObjectRequest req) {
        s3DirectoryService.putDirectoryBucketObject(req.bucketName(), req.objectKey(), req.filePath());
    }

    @Operation(summary = "列出已上传分片", description = "列出指定分片上传任务中已上传的分片信息")
    @PostMapping("/listDirectoryBucketMultipartUploadParts")
    public List<Part> listDirectoryBucketMultipartUploadParts(@Valid @RequestBody ListMultipartUploadPartsRequest req) {
        return s3DirectoryService.listDirectoryBucketMultipartUploadParts(req.bucketName(), req.objectKey(), req.uploadId());
    }

    @Operation(summary = "列出分片上传任务", description = "列出目录桶中正在进行的分片上传任务")
    @GetMapping("/listDirectoryBucketMultipartUploads")
    public List<MultipartUpload> listDirectoryBucketMultipartUploads(@Valid @RequestBody ListMultipartUploadsRequest req) {
        return s3DirectoryService.listDirectoryBucketMultipartUploads(req.bucketName());
    }

    @Operation(summary = "创建分片上传任务", description = "为大文件上传创建分片上传任务，返回 uploadId")
    @PostMapping("/createDirectoryBucketMultipartUpload")
    public String createDirectoryBucketMultipartUpload(@Valid @RequestBody CreateMultipartUploadRequest req) {
        return s3DirectoryService.createDirectoryBucketMultipartUpload(req.bucketName(), req.objectKey());
    }

    @Operation(summary = "完成分片上传", description = "提交所有已上传的分片，合并成完整对象")
    @PostMapping("/completeDirectoryBucketMultipartUpload")
    public boolean completeDirectoryBucketMultipartUpload(@Valid @RequestBody CompleteMultipartUploadRequest req) {
        return s3DirectoryService.completeDirectoryBucketMultipartUpload(req.bucketName(), req.objectKey(), req.uploadId(), req.uploadParts());
    }

    @Operation(summary = "中止分片上传", description = "根据 bucket、objectKey 和 uploadId 中止一个未完成的分片上传")
    @PostMapping("/abortDirectoryBucketMultipartUpload")
    public boolean abortDirectoryBucketMultipartUpload(@Valid @RequestBody AbortMultipartUploadRequest req) {
        return s3DirectoryService.abortDirectoryBucketMultipartUpload(req.bucketName(), req.objectKey(), req.uploadId());
    }

    @Operation(summary = "分片上传", description = "执行大文件的分片上传，返回已完成的分片信息")
    @PostMapping("/multipartUploadForDirectoryBucket")
    public List<CompletedPart> multipartUploadForDirectoryBucket(@Valid @RequestBody MultipartUploadRequest req) throws IOException {
        Path filePath = req.filePath();
        return s3DirectoryService.multipartUploadForDirectoryBucket(req.bucketName(), req.objectKey(), req.uploadId(), filePath);
    }

    @Operation(summary = "分片复制", description = "通过分片方式复制大对象到目标位置")
    @PostMapping("/multipartUploadCopyForDirectoryBucket")
    public List<CompletedPart> multipartUploadCopyForDirectoryBucket(@Valid @RequestBody MultipartUploadCopyRequest req) {
        return s3DirectoryService.multipartUploadCopyForDirectoryBucket(
                req.sourceBucket(), req.sourceKey(), req.destinationBucket(), req.destinationKey(), req.uploadId()
        );
    }
}

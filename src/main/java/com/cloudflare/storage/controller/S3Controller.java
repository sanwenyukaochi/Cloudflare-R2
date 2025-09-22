package com.cloudflare.storage.controller;

import com.cloudflare.storage.requests.BucketRequests.*;
import com.cloudflare.storage.service.S3Service;
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

    /**
     * 终止目录桶的分片上传任务。
     * @param req 包含 bucketName、objectKey、uploadId
     * @return 是否成功
     */
    @PostMapping("/abortDirectoryBucketMultipartUpload")
    public boolean abortDirectoryBucketMultipartUpload(@RequestBody AbortMultipartUploadRequest req) {
        return s3Service.abortDirectoryBucketMultipartUpload(req.bucketName(), req.objectKey(), req.uploadId());
    }

    /**
     * 完成目录桶的分片上传，提交所有已上传的分片。
     * @param req 包含 bucketName、objectKey、uploadId、uploadParts
     * @return 是否成功
     */
    @PostMapping("/completeDirectoryBucketMultipartUpload")
    public boolean completeDirectoryBucketMultipartUpload(@RequestBody CompleteMultipartUploadRequest req) {
        return s3Service.completeDirectoryBucketMultipartUpload(req.bucketName(), req.objectKey(), req.uploadId(), req.uploadParts());
    }

    /**
     * 复制对象：从 sourceBucket 的 objectKey 复制到 targetBucket。
     * @param req 包含 sourceBucket、objectKey、targetBucket
     */
    @PostMapping("/copyDirectoryBucketObject")
    public void copyDirectoryBucketObject(@RequestBody CopyObjectRequest req) {
        s3Service.copyDirectoryBucketObject(req.sourceBucket(), req.objectKey(), req.targetBucket());
    }

    /**
     * 创建目录桶，可指定可用区/区域。
     * @param req 包含 bucketName、zone
     */
    @PostMapping("/createDirectoryBucket")
    public void createDirectoryBucket(@RequestBody CreateBucketRequest req) {
        s3Service.createDirectoryBucket(req.bucketName(), req.zone());
    }

    /**
     * 创建一个目录桶的分片上传任务。
     * @param req 包含 bucketName、objectKey
     * @return 返回 uploadId
     */
    @PostMapping("/createDirectoryBucketMultipartUpload")
    public String createDirectoryBucketMultipartUpload(@RequestBody CreateMultipartUploadRequest req) {
        return s3Service.createDirectoryBucketMultipartUpload(req.bucketName(), req.objectKey());
    }


    /**
     * 删除目录桶（需保证桶为空）。
     * @param req 包含 bucketName
     */
    @DeleteMapping("/deleteDirectoryBucket")
    public void deleteDirectoryBucket(@RequestBody DeleteBucketRequest req) {
        s3Service.deleteDirectoryBucket(req.bucketName());
    }

    /**
     * 删除目录桶的加密配置。
     * @param req 包含 bucketName
     */
    @DeleteMapping("/deleteDirectoryBucketEncryption")
    public void deleteDirectoryBucketEncryption(@RequestBody DeleteBucketEncryptionRequest req) {
        s3Service.deleteDirectoryBucketEncryption(req.bucketName());
    }

    /**
     * 删除目录桶的策略（Bucket Policy）。
     * @param req 包含 bucketName
     */
    @DeleteMapping("/deleteDirectoryBucketPolicy")
    public void deleteDirectoryBucketPolicy(@RequestBody DeleteBucketPolicyRequest req) {
        s3Service.deleteDirectoryBucketPolicy(req.bucketName());
    }

    /**
     * 删除目录桶中的单个对象。
     * @param req 包含 bucketName、objectKey
     */
    @DeleteMapping("/deleteDirectoryBucketObject")
    public void deleteDirectoryBucketObject(@RequestBody DeleteObjectRequest req) {
        s3Service.deleteDirectoryBucketObject(req.bucketName(), req.objectKey());
    }

    /**
     * 批量删除目录桶中的多个对象。
     * @param req 包含 bucketName、objectKeys
     */
    @DeleteMapping("/deleteDirectoryBucketObjects")
    public void deleteDirectoryBucketObjects(@RequestBody DeleteObjectsRequest req) {
        s3Service.deleteDirectoryBucketObjects(req.bucketName(), req.objectKeys());
    }


    /**
     * 获取目录桶的加密配置。
     * @param req 包含 bucketName
     * @return 加密配置字符串（例如 KMS 相关信息）
     */
    @GetMapping("/getDirectoryBucketEncryption")
    public String getDirectoryBucketEncryption(@RequestBody GetBucketEncryptionRequest req) {
        return s3Service.getDirectoryBucketEncryption(req.bucketName());
    }


    /**
     * 获取目录桶策略（Bucket Policy）。
     * @param req 包含 bucketName
     * @return 策略文本
     */
    @GetMapping("/getDirectoryBucketPolicy")
    public String getDirectoryBucketPolicy(@RequestBody GetBucketPolicyRequest req) {
        return s3Service.getDirectoryBucketPolicy(req.bucketName());
    }


    /**
     * 获取目录桶中的对象（示例中返回布尔值表示是否成功）。
     * @param req 包含 bucketName、objectKey
     * @return 是否成功获取
     */
    @GetMapping("/getDirectoryBucketObject")
    public boolean getDirectoryBucketObject(@RequestBody GetObjectRequest req) {
        return s3Service.getDirectoryBucketObject(req.bucketName(), req.objectKey());
    }

    /**
     * 获取对象的属性信息（如大小、ETag 等，示例返回布尔值）。
     * @param req 包含 bucketName、objectKey
     * @return 是否成功获取属性
     */
    @GetMapping("/getDirectoryBucketObjectAttributes")
    public boolean getDirectoryBucketObjectAttributes(@RequestBody GetObjectAttributesRequest req) {
        return s3Service.getDirectoryBucketObjectAttributes(req.bucketName(), req.objectKey());
    }


    /**
     * 检查目录桶是否存在（HEAD Bucket）。
     * @param req 包含 bucketName
     * @return 存在返回 true
     */
    @PostMapping("/headDirectoryBucket")
    public boolean headDirectoryBucket(@RequestBody HeadBucketRequest req) {
        return s3Service.headDirectoryBucket(req.bucketName());
    }

    /**
     * 检查对象是否存在（HEAD Object）。
     * @param req 包含 bucketName、objectKey
     * @return 存在返回 true
     */
    @PostMapping("/headDirectoryBucketObject")
    public boolean headDirectoryBucketObject(@RequestBody HeadObjectRequest req) {
        return s3Service.headDirectoryBucketObject(req.bucketName(), req.objectKey());
    }

    /**
     * 列出当前账号下所有目录桶名称。
     * @return 目录桶名称列表
     */
    @GetMapping("/listDirectoryBuckets")
    public List<String> listDirectoryBuckets() {
        return s3Service.listDirectoryBuckets();
    }


    /**
     * 列出目录桶中正在进行的分片上传任务。
     * @param req 包含 bucketName
     * @return 分片上传任务列表
     */
    @GetMapping("/listDirectoryBucketMultipartUploads")
    public List<MultipartUpload> listDirectoryBucketMultipartUploads(@RequestBody ListMultipartUploadsRequest req) {
        return s3Service.listDirectoryBucketMultipartUploads(req.bucketName());
    }


    /**
     * 列出目录桶中的对象（ListObjectsV2）。
     * @param req 包含 bucketName
     * @return 对象键列表
     */
    @GetMapping("/listDirectoryBucketObjectsV2")
    public List<String> listDirectoryBucketObjectsV2(@RequestBody ListObjectsV2Request req) {
        return s3Service.listDirectoryBucketObjectsV2(req.bucketName());
    }


    /**
     * 列出某个分片上传任务已上传的分片列表。
     * @param req 包含 bucketName、objectKey、uploadId
     * @return 已上传分片信息列表
     */
    @PostMapping("/listDirectoryBucketMultipartUploadParts")
    public List<Part> listDirectoryBucketMultipartUploadParts(@RequestBody ListMultipartUploadPartsRequest req) {
        return s3Service.listDirectoryBucketMultipartUploadParts(req.bucketName(), req.objectKey(), req.uploadId());
    }


    /**
     * 设置目录桶的加密配置（例如 KMS Key）。
     * @param req 包含 bucketName、kmsKeyId
     */
    @PutMapping("/putDirectoryBucketEncryption")
    public void putDirectoryBucketEncryption(@RequestBody PutBucketEncryptionRequest req) {
        s3Service.putDirectoryBucketEncryption(req.bucketName(), req.kmsKeyId());
    }

    /**
     * 设置目录桶策略（Bucket Policy）。
     * @param req 包含 bucketName、policyText
     */
    @PutMapping("/putDirectoryBucketPolicy")
    public void putDirectoryBucketPolicy(@RequestBody PutBucketPolicyRequest req) {
        s3Service.putDirectoryBucketPolicy(req.bucketName(), req.policyText());
    }

    /**
     * 上传对象（单次/普通上传），从本地文件路径读取内容。
     * @param req 包含 bucketName、objectKey、filePath
     */
    @PutMapping("/putDirectoryBucketObject")
    public void putDirectoryBucketObject(@RequestBody PutObjectRequest req) {
        s3Service.putDirectoryBucketObject(req.bucketName(), req.objectKey(), req.filePath());
    }

    /**
     * 进行分片上传（适合大文件），返回每个已完成分片的信息。
     * @param req 包含 bucketName、objectKey、uploadId、filePath
     * @return 已完成分片列表
     */
    @PostMapping("/multipartUploadForDirectoryBucket")
    public List<CompletedPart> multipartUploadForDirectoryBucket(@RequestBody MultipartUploadRequest req) throws IOException {
        Path filePath = req.filePath();
        return s3Service.multipartUploadForDirectoryBucket(req.bucketName(), req.objectKey(), req.uploadId(), filePath);
    }

    /**
     * 分片拷贝（服务端完成），按分片从源对象复制到目标对象。
     * @param req 包含 sourceBucket、sourceKey、destinationBucket、destinationKey、uploadId
     * @return 已完成分片列表
     */
    @PostMapping("/multipartUploadCopyForDirectoryBucket")
    public List<CompletedPart> multipartUploadCopyForDirectoryBucket(@RequestBody MultipartUploadCopyRequest req) {
        return s3Service.multipartUploadCopyForDirectoryBucket(
                req.sourceBucket(), req.sourceKey(), req.destinationBucket(), req.destinationKey(), req.uploadId()
        );
    }
}

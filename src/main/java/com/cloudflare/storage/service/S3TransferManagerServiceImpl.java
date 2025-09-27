//package com.cloudflare.storage.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
//import software.amazon.awssdk.transfer.s3.S3TransferManager;
//import software.amazon.awssdk.transfer.s3.model.CompletedCopy;
//import software.amazon.awssdk.transfer.s3.model.Copy;
//import software.amazon.awssdk.transfer.s3.model.CopyRequest;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class S3TransferManagerServiceImpl {
//
//    private final S3TransferManager transferManager;
//
//    public String copyObject(String sourceBucket, String sourceObjectKey, String targetBucket, String targetObjectKey) {
//        CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
//                .sourceBucket(sourceBucket)
//                .sourceKey(sourceObjectKey)
//                .destinationBucket(targetBucket)
//                .destinationKey(targetObjectKey)
//                .build();
//
//        CopyRequest copyRequest = CopyRequest.builder()
//                .copyObjectRequest(copyObjectRequest)
//                .build();
//
//        Copy copy = transferManager.copy(copyRequest);
//
//        CompletedCopy completedCopy = copy.completionFuture().join();
//        return completedCopy.response().copyObjectResult().eTag();
//    }
//}

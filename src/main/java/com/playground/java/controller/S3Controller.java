package com.playground.java.controller;

import com.playground.java.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @GetMapping("/buckets")
    public List<String> getBuckets() {
        return s3Service.listDirectoryBuckets();
    }

    @GetMapping("/buckets/{bucketName}/objects")
    public List<String> getObjects(@PathVariable String bucketName) {
        return s3Service.listDirectoryBucketObjectsV2(bucketName);
    }
}

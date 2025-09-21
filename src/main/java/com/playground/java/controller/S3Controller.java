package com.playground.java.controller;

import com.playground.java.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping("/createDirectoryBucket/{bucketName}")
    public void createDirectoryBucket(@PathVariable String bucketName, @RequestParam(defaultValue = "auto") String zone) {
        s3Service.createDirectoryBucket(bucketName, zone);
    }
    
    @GetMapping("/listDirectoryBuckets")
    public List<String> listDirectoryBuckets() {
        return s3Service.listDirectoryBuckets();
    }

    @GetMapping("/listDirectoryBucketObjectsV2/{bucketName}")
    public List<String> listDirectoryBucketObjectsV2(@PathVariable String bucketName) {
        return s3Service.listDirectoryBucketObjectsV2(bucketName);
    }
}

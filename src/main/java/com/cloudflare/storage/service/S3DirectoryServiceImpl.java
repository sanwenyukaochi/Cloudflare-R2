package com.cloudflare.storage.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3DirectoryServiceImpl implements S3DirectoryService {
    private final S3Client s3Client;

    /**
     * Aborts a specific multipart upload for the specified S3 directory bucket.
     *
     * @param bucketName The name of the directory bucket
     * @param objectKey  The key (name) of the object to be uploaded
     * @param uploadId   The upload ID of the multipart upload to abort
     * @return True if the multipart upload is successfully aborted, false otherwise
     */
    @Override
    public boolean abortDirectoryBucketMultipartUpload(String bucketName, String objectKey, String uploadId) {
        log.info("Aborting multipart upload: {} for bucket: {}", uploadId, bucketName);
        try {
            // Abort the multipart upload
            AbortMultipartUploadRequest abortMultipartUploadRequest = AbortMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .uploadId(uploadId)
                    .build();

            s3Client.abortMultipartUpload(abortMultipartUploadRequest);
            log.info("Aborted multipart upload: {} for object: {}", uploadId, objectKey);
            return true;
        } catch (S3Exception e) {
            log.error("Failed to abort multipart upload: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
            return false;
        }
    }

    /**
     * This method completes the multipart upload request by collating all the
     * upload parts.
     *
     * @param bucketName  The name of the directory bucket
     * @param objectKey   The key (name) of the object to be uploaded
     * @param uploadId    The upload ID used to track the multipart upload
     * @param uploadParts The list of completed parts
     * @return True if the multipart upload is successfully completed, false
     * otherwise
     */
    @Override
    public boolean completeDirectoryBucketMultipartUpload(String bucketName, String objectKey, String uploadId, List<CompletedPart> uploadParts) {
        try {
            CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                    .parts(uploadParts)
                    .build();
            CompleteMultipartUploadRequest completeMultipartUploadRequest = CompleteMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .uploadId(uploadId)
                    .multipartUpload(completedMultipartUpload)
                    .build();

            CompleteMultipartUploadResponse response = s3Client.completeMultipartUpload(completeMultipartUploadRequest);
            log.info("Multipart upload completed. ETag: {}", response.eTag());
            return true;
        } catch (S3Exception e) {
            log.error("Failed to complete multipart upload: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
            return false;
        }
    }

    /**
     * Copies an object from one S3 general purpose bucket to one S3 directory
     * bucket.
     *
     * @param sourceBucket The name of the source bucket
     * @param sourceObjectKey  The key (name) of the source object
     * @param targetBucket The name of the target bucket
     * @param targetObjectKey  The key (name) of the target object
     */
    @Override
    public void copyDirectoryBucketObject(String sourceBucket, String sourceObjectKey, String targetBucket, String targetObjectKey) {
        log.info("Copying object: {} from bucket: {} to bucket: {} with new key: {}", sourceObjectKey, sourceBucket, targetBucket, targetObjectKey);

        try {
            // Create a CopyObjectRequest
            CopyObjectRequest copyReq = CopyObjectRequest.builder()
                    .sourceBucket(sourceBucket)
                    .sourceKey(sourceObjectKey)
                    .destinationBucket(targetBucket)
                    .destinationKey(targetObjectKey)
                    .build();

            // Copy the object
            CopyObjectResponse copyRes = s3Client.copyObject(copyReq);
            log.info("Successfully copied {} from bucket {} into bucket {} as {}. CopyObjectResponse: {}",
                    sourceObjectKey, sourceBucket, targetBucket, targetObjectKey, copyRes.copyObjectResult().toString());

        } catch (S3Exception e) {
            log.error("Failed to copy object: {} from bucket {} to bucket {} - Error code: {}, message: {}", sourceObjectKey, sourceBucket, targetBucket,
                    e.awsErrorDetails().errorCode(), e.awsErrorDetails().errorMessage(), e);
            throw e;
        }
    }

    /**
     * Creates a new S3 directory bucket in a specified Zone (For example, a
     * specified Availability Zone in this code example).
     *
     * @param bucketName The name of the bucket to be created
     * @param zone       The region where the bucket will be created
     * @throws S3Exception if there's an error creating the bucket
     */
    @Override
    public void createDirectoryBucket(String bucketName, String zone) throws S3Exception {
        log.info("Creating bucket: {}", bucketName);

        CreateBucketConfiguration bucketConfiguration = CreateBucketConfiguration.builder()
                .location(LocationInfo.builder()
                        .type(LocationType.AVAILABILITY_ZONE)
                        .name(zone).build())
                .bucket(BucketInfo.builder()
                        .type(BucketType.DIRECTORY)
                        .dataRedundancy(DataRedundancy.SINGLE_AVAILABILITY_ZONE)
                        .build())
                .build();
        try {
            CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .createBucketConfiguration(bucketConfiguration).build();
            CreateBucketResponse response = s3Client.createBucket(bucketRequest);
            log.info("Bucket created successfully with location: {}", response.location());
        } catch (S3Exception e) {
            log.error("Error creating bucket: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
            throw e;
        }
    }

    /**
     * This method creates a multipart upload request that generates a unique upload
     * ID used to track
     * all the upload parts.
     *
     * @param bucketName The name of the directory bucket
     * @param objectKey  The key (name) of the object to be uploaded
     * @return The upload ID used to track the multipart upload
     */
    @Override
    public String createDirectoryBucketMultipartUpload(String bucketName, String objectKey) {
        log.info("Creating multipart upload for object: {} in bucket: {}", objectKey, bucketName);

        try {
            // Create a CreateMultipartUploadRequest
            CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            // Initiate the multipart upload
            CreateMultipartUploadResponse response = s3Client.createMultipartUpload(createMultipartUploadRequest);
            String uploadId = response.uploadId();
            log.info("Multipart upload initiated. Upload ID: {}", uploadId);
            return uploadId;

        } catch (S3Exception e) {
            log.error("Failed to create multipart upload: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
            throw e;
        }
    }

    /**
     * Deletes the specified S3 directory bucket.
     *
     * @param bucketName The name of the directory bucket to delete
     */
    @Override
    public void deleteDirectoryBucket(String bucketName) {
        log.info("Deleting bucket: {}", bucketName);

        try {
            // Create a DeleteBucketRequest
            DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            // Delete the bucket
            s3Client.deleteBucket(deleteBucketRequest);
            log.info("Successfully deleted bucket: {}", bucketName);

        } catch (S3Exception e) {
            log.error("Failed to delete bucket: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
            throw e;
        }
    }

    /**
     * Deletes the encryption configuration from an S3 bucket.
     *
     * @param bucketName The name of the directory bucket
     */
    @Override
    public void deleteDirectoryBucketEncryption(String bucketName) {
        DeleteBucketEncryptionRequest deleteRequest = DeleteBucketEncryptionRequest.builder()
                .bucket(bucketName)
                .build();

        try {
            s3Client.deleteBucketEncryption(deleteRequest);
            log.info("Bucket encryption deleted for bucket: {}", bucketName);
        } catch (S3Exception e) {
            log.error("Failed to delete bucket encryption: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
            throw e;
        }
    }

    /**
     * Deletes the bucket policy for the specified S3 directory bucket.
     *
     * @param bucketName The name of the directory bucket
     */
    @Override
    public void deleteDirectoryBucketPolicy(String bucketName) {
        log.info("Deleting policy for bucket: {}", bucketName);

        try {
            // Create a DeleteBucketPolicyRequest
            DeleteBucketPolicyRequest deletePolicyReq = DeleteBucketPolicyRequest.builder()
                    .bucket(bucketName)
                    .build();

            // Delete the bucket policy
            s3Client.deleteBucketPolicy(deletePolicyReq);
            log.info("Successfully deleted bucket policy");

        } catch (S3Exception e) {
            log.error("Failed to delete bucket policy: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
            throw e;
        }
    }

    /**
     * Deletes an object from the specified S3 directory bucket.
     *
     * @param bucketName The name of the directory bucket
     * @param objectKey  The key (name) of the object to be deleted
     */
    @Override
    public void deleteDirectoryBucketObject(String bucketName, String objectKey) {
        log.info("Deleting object: {} from bucket: {}", objectKey, bucketName);

        try {
            // Create a DeleteObjectRequest
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            // Delete the object
            s3Client.deleteObject(deleteObjectRequest);
            log.info("Object {} has been deleted", objectKey);

        } catch (S3Exception e) {
            log.error("Failed to delete object: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
            throw e;
        }
    }

    /**
     * Deletes multiple objects from the specified S3 directory bucket.
     *
     * @param bucketName The name of the directory bucket
     * @param objectKeys The list of keys (names) of the objects to be deleted
     */
    @Override
    public void deleteDirectoryBucketObjects(String bucketName, List<String> objectKeys) {
        log.info("Deleting objects from bucket: {}", bucketName);

        try {
            // Create a list of ObjectIdentifier.
            List<ObjectIdentifier> identifiers = objectKeys.stream()
                    .map(key -> ObjectIdentifier.builder().key(key).build())
                    .toList();

            Delete delete = Delete.builder()
                    .objects(identifiers)
                    .build();

            DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(delete)
                    .build();

            DeleteObjectsResponse deleteObjectsResponse = s3Client.deleteObjects(deleteObjectsRequest);
            deleteObjectsResponse.deleted().forEach(deleted -> log.info("Deleted object: {}", deleted.key()));

        } catch (S3Exception e) {
            log.error("Failed to delete objects: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
            throw e;
        }
    }

    /**
     * Retrieves the encryption configuration for an S3 directory bucket.
     *
     * @param bucketName The name of the directory bucket
     * @return The type of server-side encryption applied to the bucket (e.g.,
     * AES256, aws:kms)
     */
    @Override
    public String getDirectoryBucketEncryption(String bucketName) {
        try {
            // Create a GetBucketEncryptionRequest
            GetBucketEncryptionRequest getRequest = GetBucketEncryptionRequest.builder()
                    .bucket(bucketName)
                    .build();

            // Retrieve the bucket encryption configuration
            GetBucketEncryptionResponse response = s3Client.getBucketEncryption(getRequest);
            ServerSideEncryptionRule rule = response.serverSideEncryptionConfiguration().rules().get(0);

            String encryptionType = rule.applyServerSideEncryptionByDefault().sseAlgorithmAsString();
            log.info("Bucket encryption algorithm: {}", encryptionType);
            log.info("KMS Customer Managed Key ID: {}", rule.applyServerSideEncryptionByDefault().kmsMasterKeyID());
            log.info("Bucket Key Enabled: {}", rule.bucketKeyEnabled());

            return encryptionType;
        } catch (S3Exception e) {
            log.error("Failed to get bucket encryption: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
            throw e;
        }
    }

    /**
     * Retrieves the bucket policy for the specified S3 directory bucket.
     *
     * @param bucketName The name of the directory bucket
     * @return The bucket policy text
     */
    @Override
    public String getDirectoryBucketPolicy(String bucketName) {
        log.info("Getting policy for bucket: {}", bucketName);

        try {
            // Create a GetBucketPolicyRequest
            GetBucketPolicyRequest policyReq = GetBucketPolicyRequest.builder()
                    .bucket(bucketName)
                    .build();

            // Retrieve the bucket policy
            GetBucketPolicyResponse response = s3Client.getBucketPolicy(policyReq);

            // Print and return the policy text
            String policyText = response.policy();
            log.info("Bucket policy: {}", policyText);
            return policyText;

        } catch (S3Exception e) {
            log.error("Failed to get bucket policy: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
            throw e;
        }
    }

    /**
     * Retrieves an object from the specified S3 directory bucket.
     *
     * @param bucketName The name of the directory bucket
     * @param objectKey  The key (name) of the object to be retrieved
     * @return The retrieved object as a ResponseInputStream
     */
    @Override
    public boolean getDirectoryBucketObject(String bucketName, String objectKey) {
        log.info("Retrieving object: {} from bucket: {}", objectKey, bucketName);

        try {
            // Create a GetObjectRequest
            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .key(objectKey)
                    .bucket(bucketName)
                    .build();

            // Retrieve the object as bytes
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(objectRequest);
            byte[] data = objectBytes.asByteArray();

            // Print object contents to console
            String objectContent = new String(data, StandardCharsets.UTF_8);
            log.info("Object contents: \n{}", objectContent);

            return true;

        } catch (S3Exception e) {
            log.error("Failed to retrieve object: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
            return false;
        }
    }

    /**
     * Retrieves attributes for an object in the specified S3 directory bucket.
     *
     * @param bucketName The name of the directory bucket
     * @param objectKey  The key (name) of the object to retrieve attributes for
     * @return True if the object attributes are successfully retrieved, false
     * otherwise
     */
    @Override
    public boolean getDirectoryBucketObjectAttributes(String bucketName, String objectKey) {
        log.info("Retrieving attributes for object: {} from bucket: {}", objectKey, bucketName);

        try {
            // Create a GetObjectAttributesRequest
            GetObjectAttributesRequest getObjectAttributesRequest = GetObjectAttributesRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .objectAttributes(ObjectAttributes.E_TAG, ObjectAttributes.STORAGE_CLASS,
                            ObjectAttributes.OBJECT_SIZE)
                    .build();

            // Retrieve the object attributes
            GetObjectAttributesResponse response = s3Client.getObjectAttributes(getObjectAttributesRequest);
            log.info("Attributes for object {}:", objectKey);
            log.info("ETag: {}", response.eTag());
            log.info("Storage Class: {}", response.storageClass());
            log.info("Object Size: {}", response.objectSize());
            return true;

        } catch (S3Exception e) {
            log.error("Failed to retrieve object attributes: {} - Error code: {}",
                    e.awsErrorDetails().errorMessage(), e.awsErrorDetails().errorCode(), e);
            return false;
        }
    }

    /**
     * Checks if the specified S3 directory bucket exists and is accessible.
     *
     * @param bucketName The name of the directory bucket to check
     * @return True if the bucket exists and is accessible, false otherwise
     */
    @Override
    public boolean headDirectoryBucket(String bucketName) {
        log.info("Checking if bucket exists: {}", bucketName);

        try {
            // Create a HeadBucketRequest
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            // If the bucket doesn't exist, the following statement throws NoSuchBucketException,
            // which is a subclass of S3Exception.
            s3Client.headBucket(headBucketRequest);
            log.info("Amazon S3 directory bucket: \"{}\" found.", bucketName);
            return true;

        } catch (S3Exception e) {
            log.error("Failed to access bucket: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
            throw e;
        }
    }

    /**
     * Retrieves metadata for an object in the specified S3 directory bucket.
     *
     * @param bucketName The name of the directory bucket
     * @param objectKey  The key (name) of the object to retrieve metadata for
     * @return True if the object exists, false otherwise
     */
    @Override
    public boolean headDirectoryBucketObject(String bucketName, String objectKey) {
        log.info("Retrieving metadata for object: {} from bucket: {}", objectKey, bucketName);

        try {
            // Create a HeadObjectRequest
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            // Retrieve the object metadata
            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            log.info("Amazon S3 object: \"{}\" found in bucket: \"{}\" with ETag: \"{}\"", objectKey, bucketName,
                    response.eTag());
            log.info("Content-Type: {}", response.contentType());
            log.info("Content-Length: {}", response.contentLength());
            log.info("Last Modified: {}", response.lastModified());
            return true;

        } catch (S3Exception e) {
            log.error("Failed to retrieve object metadata: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
            return false;
        }
    }

    /**
     * Lists all S3 directory buckets and no general purpose buckets.
     *
     * @return A list of bucket names
     */
    @Override
    public List<String> listDirectoryBuckets() {
        log.info("Listing all directory buckets");

        try {
            // Create a ListBucketsRequest
            ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();

            // Retrieve the list of buckets
            ListBucketsResponse response = s3Client.listBuckets(listBucketsRequest);

            // Extract bucket names
            List<String> bucketNames = response.buckets().stream()
                    .map(Bucket::name)
                    .collect(Collectors.toList());

            return bucketNames;
        } catch (S3Exception e) {
            log.error("Failed to list buckets: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode());
            throw e;
        }
    }

    /**
     * Lists multipart uploads for the specified S3 directory bucket.
     *
     * @param bucketName The name of the directory bucket
     * @return A list of MultipartUpload objects representing the multipart uploads
     */
    @Override
    public List<MultipartUpload> listDirectoryBucketMultipartUploads(String bucketName) {
        log.info("Listing in-progress multipart uploads for bucket: {}", bucketName);

        try {
            // Create a ListMultipartUploadsRequest
            ListMultipartUploadsRequest listMultipartUploadsRequest = ListMultipartUploadsRequest.builder()
                    .bucket(bucketName)
                    .build();

            // List the multipart uploads
            ListMultipartUploadsResponse response = s3Client.listMultipartUploads(listMultipartUploadsRequest);
            List<MultipartUpload> uploads = response.uploads();
            for (MultipartUpload upload : uploads) {
                log.info("In-progress multipart upload: Upload ID: {}, Key: {}, Initiated: {}", upload.uploadId(),
                        upload.key(), upload.initiated());
            }
            return uploads;

        } catch (S3Exception e) {
            log.error("Failed to list multipart uploads: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode());
            return List.of(); // Return an empty list if an exception is thrown
        }
    }

    /**
     * Lists objects in the specified S3 directory bucket.
     *
     * @param bucketName The name of the directory bucket
     * @return A list of object keys in the bucket
     */
    @Override
    public List<String> listDirectoryBucketObjectsV2(String bucketName) {
        log.info("Listing objects in bucket: {}", bucketName);

        try {
            // Create a ListObjectsV2Request
            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            // Retrieve the list of objects
            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);

            // Extract and return the object keys
            return response.contents().stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());

        } catch (S3Exception e) {
            log.error("Failed to list objects: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode());
            throw e;
        }
    }

    /**
     * Lists the parts of a multipart upload for the specified S3 directory bucket.
     *
     * @param bucketName The name of the directory bucket
     * @param objectKey  The key (name) of the object being uploaded
     * @param uploadId   The upload ID used to track the multipart upload
     * @return A list of Part representing the parts of the multipart upload
     */
    @Override
    public List<Part> listDirectoryBucketMultipartUploadParts(String bucketName, String objectKey, String uploadId) {
        log.info("Listing parts for object: {} in bucket: {}", objectKey, bucketName);

        try {
            // Create a ListPartsRequest
            ListPartsRequest listPartsRequest = ListPartsRequest.builder()
                    .bucket(bucketName)
                    .uploadId(uploadId)
                    .key(objectKey)
                    .build();

            // List the parts of the multipart upload
            ListPartsResponse response = s3Client.listParts(listPartsRequest);
            List<Part> parts = response.parts();
            for (Part part : parts) {
                log.info("Uploaded part: Part number = \"{}\", etag = {}", part.partNumber(), part.eTag());
            }
            return parts;

        } catch (S3Exception e) {
            log.error("Failed to list parts: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode());
            return List.of(); // Return an empty list if an exception is thrown
        }
    }

    /**
     * Sets the default encryption configuration for an S3 bucket as SSE-KMS.
     *
     * @param bucketName The name of the directory bucket
     * @param kmsKeyId   The ID of the customer-managed KMS key
     */
    @Override
    public void putDirectoryBucketEncryption(String bucketName, String kmsKeyId) {
        // Define the default encryption configuration to use SSE-KMS. For directory
        // buckets, AWS managed KMS keys aren't supported. Only customer-managed keys
        // are supported.
        ServerSideEncryptionByDefault encryptionByDefault = ServerSideEncryptionByDefault.builder()
                .sseAlgorithm(ServerSideEncryption.AWS_KMS)
                .kmsMasterKeyID(kmsKeyId)
                .build();

        // Create a server-side encryption rule to apply the default encryption
        // configuration. For directory buckets, the bucketKeyEnabled field is enforced
        // to be true.
        ServerSideEncryptionRule rule = ServerSideEncryptionRule.builder()
                .bucketKeyEnabled(true)
                .applyServerSideEncryptionByDefault(encryptionByDefault)
                .build();

        // Create the server-side encryption configuration for the bucket
        ServerSideEncryptionConfiguration encryptionConfiguration = ServerSideEncryptionConfiguration.builder()
                .rules(rule)
                .build();

        // Create the PutBucketEncryption request
        PutBucketEncryptionRequest putRequest = PutBucketEncryptionRequest.builder()
                .bucket(bucketName)
                .serverSideEncryptionConfiguration(encryptionConfiguration)
                .build();

        // Set the bucket encryption
        try {
            s3Client.putBucketEncryption(putRequest);
            log.info("SSE-KMS Bucket encryption configuration set for the directory bucket: {}", bucketName);
        } catch (S3Exception e) {
            log.error("Failed to set bucket encryption: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode());
            throw e;
        }
    }

    /**
     * Sets the following bucket policy for the specified S3 directory bucket.
     * <pre>
     * {
     *     "Version": "2012-10-17",
     *     "Statement": [
     *         {
     *             "Sid": "AdminPolicy",
     *             "Effect": "Allow",
     *             "Principal": {
     *                 "AWS": "arn:aws:iam::<ACCOUNT_ID>:root"
     *             },
     *             "Action": "s3express:*",
     *             "Resource": "arn:aws:s3express:us-west-2:<ACCOUNT_ID>:bucket/<DIR_BUCKET_NAME>
     *         }
     *     ]
     * }
     * </pre>
     * This policy grants all S3 directory bucket actions to identities in the same account as the bucket.
     *
     * @param bucketName The name of the directory bucket
     * @param policyText The policy text to be applied
     */
    @Override
    public void putDirectoryBucketPolicy(String bucketName, String policyText) {
        log.info("Setting policy on bucket: {}", bucketName);
        log.info("Policy: {}", policyText);

        try {
            PutBucketPolicyRequest policyReq = PutBucketPolicyRequest.builder()
                    .bucket(bucketName)
                    .policy(policyText)
                    .build();

            s3Client.putBucketPolicy(policyReq);
            log.info("Bucket policy set successfully!");

        } catch (S3Exception e) {
            log.error("Failed to set bucket policy: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
            throw e;
        }
    }

    /**
     * Puts an object into the specified S3 directory bucket.
     *
     * @param bucketName The name of the directory bucket
     * @param objectKey  The key (name) of the object to be placed in the bucket
     * @param filePath   The path of the file to be uploaded
     */
    @Override
    public void putDirectoryBucketObject(String bucketName, String objectKey, Path filePath) {
        log.info("Putting object: {} into bucket: {}", objectKey, bucketName);

        try {
            // Create a PutObjectRequest
            PutObjectRequest putObj = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            // Upload the object
            s3Client.putObject(putObj, filePath);
            log.info("Successfully placed {} into bucket {}", objectKey, bucketName);

        } catch (UncheckedIOException e) {
            throw S3Exception.builder().message("Failed to read the file: " + e.getMessage()).cause(e)
                    .awsErrorDetails(AwsErrorDetails.builder()
                            .errorCode("ClientSideException:FailedToReadFile")
                            .errorMessage(e.getMessage())
                            .build())
                    .build();
        } catch (S3Exception e) {
            log.error("Failed to put object: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * This method creates part requests and uploads individual parts to S3.
     * While it uses the UploadPart API to upload a single part, it does so
     * sequentially to handle multiple parts of a file, returning all the completed
     * parts.
     *
     * @param bucketName The name of the directory bucket
     * @param objectKey  The key (name) of the object to be uploaded
     * @param uploadId   The upload ID used to track the multipart upload
     * @param filePath   The path to the file to be uploaded
     * @return A list of uploaded parts
     * @throws IOException if an I/O error occurs
     */
    @Override
    public List<CompletedPart> multipartUploadForDirectoryBucket(String bucketName, String objectKey, String uploadId, Path filePath) throws IOException {
        log.info("Uploading parts for object: {} in bucket: {}", objectKey, bucketName);

        int partNumber = 1;
        List<CompletedPart> uploadedParts = new ArrayList<>();
        ByteBuffer bb = ByteBuffer.allocate(1024 * 1024 * 5); // 5 MB byte buffer

        // Read the local file, break down into chunks and process
        try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "r")) {
            long fileSize = file.length();
            int position = 0;

            // Sequentially upload parts of the file
            while (position < fileSize) {
                file.seek(position);
                int read = file.getChannel().read(bb);

                bb.flip(); // Swap position and limit before reading from the buffer
                UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .uploadId(uploadId)
                        .partNumber(partNumber)
                        .build();

                UploadPartResponse partResponse = s3Client.uploadPart(
                        uploadPartRequest,
                        RequestBody.fromByteBuffer(bb));

                // Build the uploaded part
                CompletedPart uploadedPart = CompletedPart.builder()
                        .partNumber(partNumber)
                        .eTag(partResponse.eTag())
                        .build();

                // Add the uploaded part to the list
                uploadedParts.add(uploadedPart);

                // Log to indicate the part upload is done
                log.info("Uploaded part number: {} with ETag: {}", partNumber, partResponse.eTag());

                bb.clear();
                position += read;
                partNumber++;
            }
        } catch (S3Exception e) {
            log.error("Failed to list parts: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode());
            throw e;
        }
        return uploadedParts;
    }

    /**
     * Creates copy parts based on source object size and copies over individual
     * parts.
     *
     * @param sourceBucket      The name of the source bucket
     * @param sourceKey         The key (name) of the source object
     * @param destinationBucket The name of the destination bucket
     * @param destinationKey    The key (name) of the destination object
     * @param uploadId          The upload ID used to track the multipart upload
     * @return A list of completed parts
     */
    @Override
    public List<CompletedPart> multipartUploadCopyForDirectoryBucket(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey, String uploadId) {
        // Get the object size to track the end of the copy operation
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(sourceBucket)
                .key(sourceKey)
                .build();
        HeadObjectResponse headObjectResponse = s3Client.headObject(headObjectRequest);
        long objectSize = headObjectResponse.contentLength();

        log.info("Source Object size: {}", objectSize);

        // Copy the object using 20 MB parts
        long partSize = 20 * 1024 * 1024; // 20 MB
        long bytePosition = 0;
        int partNum = 1;
        List<CompletedPart> uploadedParts = new ArrayList<>();

        while (bytePosition < objectSize) {
            long lastByte = Math.min(bytePosition + partSize - 1, objectSize - 1);
            log.info("Part Number: {}, Byte Position: {}, Last Byte: {}", partNum, bytePosition, lastByte);

            try {
                UploadPartCopyRequest uploadPartCopyRequest = UploadPartCopyRequest.builder()
                        .sourceBucket(sourceBucket)
                        .sourceKey(sourceKey)
                        .destinationBucket(destinationBucket)
                        .destinationKey(destinationKey)
                        .uploadId(uploadId)
                        .copySourceRange("bytes=" + bytePosition + "-" + lastByte)
                        .partNumber(partNum)
                        .build();
                UploadPartCopyResponse uploadPartCopyResponse = s3Client.uploadPartCopy(uploadPartCopyRequest);

                CompletedPart part = CompletedPart.builder()
                        .partNumber(partNum)
                        .eTag(uploadPartCopyResponse.copyPartResult().eTag())
                        .build();
                uploadedParts.add(part);

                bytePosition += partSize;
                partNum++;
            } catch (S3Exception e) {
                log.error("Failed to copy part number {}: {} - Error code: {}", partNum,
                        e.awsErrorDetails().errorMessage(), e.awsErrorDetails().errorCode());
                throw e;
            }
        }

        return uploadedParts;
    }

    /**
     * Retrieves the URL for a specific object in an Amazon S3 bucket.
     *
     * @param bucketName the name of the S3 bucket where the object is stored
     * @param keyName the name of the object for which the URL should be retrieved
     * @throws S3Exception if there is an error retrieving the URL for the specified object
     */
    @Override
    public String getObjectUrlForDirectoryBucket(String bucketName, String keyName) {
        try {
            GetUrlRequest request = GetUrlRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            URL url = s3Client.utilities().getUrl(request);
            log.info("The URL for  {} is {}", keyName, url);
            return url.toString();
        } catch (S3Exception e) {
            log.error(e.awsErrorDetails().errorMessage());
            System.exit(1);
            return null;
        }
    }

    /**
     * Lists the tags associated with an Amazon S3 object.
     *
     * @param bucketName the name of the S3 bucket that contains the object
     * @param keyName the key (name) of the S3 object
     */
    @Override
    public void listTags(String bucketName, String keyName) {
        try {
            GetObjectTaggingRequest getTaggingRequest = GetObjectTaggingRequest
                    .builder()
                    .key(keyName)
                    .bucket(bucketName)
                    .build();

            GetObjectTaggingResponse tags = s3Client.getObjectTagging(getTaggingRequest);
            List<Tag> tagSet = tags.tagSet();
            for (Tag tag : tagSet) {
                System.out.println(tag.key());
                System.out.println(tag.value());
            }

        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    /**
     * Retrieves the bytes of an object stored in an Amazon S3 bucket and saves them to a local file.
     *
     * @param bucketName The name of the S3 bucket where the object is stored.
     * @param keyName The key (or name) of the S3 object.
     * @param path The local file path where the object's bytes will be saved.
     * @throws S3Exception If an error occurs while retrieving the object from the S3 bucket.
     */
    @Override
    public void getObjectBytes(String bucketName, String keyName, String path) {
        try {
            GetObjectRequest objectRequest = GetObjectRequest
                    .builder()
                    .key(keyName)
                    .bucket(bucketName)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObject(objectRequest, ResponseTransformer.toBytes());
            byte[] data = objectBytes.asByteArray();

            // Write the data to a local file.
            File myFile = new File(path);
            OutputStream os = new FileOutputStream(myFile);
            os.write(data);
            System.out.println("Successfully obtained bytes from an S3 object");
            os.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    // Get the legal hold details for an S3 object.
    @Override
    public ObjectLockLegalHold getObjectLegalHold(String bucketName, String objectKey) {
        try {
            GetObjectLegalHoldRequest legalHoldRequest = GetObjectLegalHoldRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            GetObjectLegalHoldResponse response = s3Client.getObjectLegalHold(legalHoldRequest);
            System.out.println("Object legal hold for " + objectKey + " in " + bucketName +
                    ":\n\tStatus: " + response.legalHold().status());
            return response.legalHold();

        } catch (S3Exception ex) {
            System.out.println("\tUnable to fetch legal hold: '" + ex.getMessage() + "'");
        }

        return null;
    }

    // Get the object lock configuration details for an S3 bucket.
    @Override
    public void getBucketObjectLockConfiguration(String bucketName) {
        GetObjectLockConfigurationRequest objectLockConfigurationRequest = GetObjectLockConfigurationRequest.builder()
                .bucket(bucketName)
                .build();

        GetObjectLockConfigurationResponse response = s3Client.getObjectLockConfiguration(objectLockConfigurationRequest);
        System.out.println("Bucket object lock config for "+bucketName +":  ");
        System.out.println("\tEnabled: "+response.objectLockConfiguration().objectLockEnabled());
        System.out.println("\tRule: "+ response.objectLockConfiguration().rule().defaultRetention());
    }

    // Get the retention period for an S3 object.
    @Override
    public ObjectLockRetention getObjectRetention(String bucketName, String key){
        try {
            GetObjectRetentionRequest retentionRequest = GetObjectRetentionRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectRetentionResponse response = s3Client.getObjectRetention(retentionRequest);
            System.out.println("tObject retention for "+key +" in "+ bucketName +": " + response.retention().mode() +" until "+ response.retention().retainUntilDate() +".");
            return response.retention();

        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            return null;
        }
    }

    /**
     * Lists the objects in the specified S3 bucket.
     *
     * @param bucketName the name of the S3 bucket to list the objects from
     */
    @Override
    public void listBucketObjects(String bucketName) {
        try {
            ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .maxKeys(1)
                    .build();

            ListObjectsV2Iterable listRes = s3Client.listObjectsV2Paginator(listReq);
            listRes.stream()
                    .flatMap(r -> r.contents().stream())
                    .forEach(content -> System.out.println(" Key: " + content.key() + " size = " + content.size()));

        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    /**
     * Sets the Access Control List (ACL) for an Amazon S3 bucket.
     *
     * @param bucketName the name of the S3 bucket to set the ACL for
     * @param id the ID of the AWS user or account that will be granted full control of the bucket
     * @throws S3Exception if an error occurs while setting the bucket ACL
     */
    @Override
    public void setBucketAcl(String bucketName, String id) {
        try {
            Grant ownerGrant = Grant.builder()
                    .grantee(builder -> builder.id(id)
                            .type(Type.CANONICAL_USER))
                    .permission(Permission.FULL_CONTROL)
                    .build();

            List<Grant> grantList2 = new ArrayList<>();
            grantList2.add(ownerGrant);

            AccessControlPolicy acl = AccessControlPolicy.builder()
                    .owner(builder -> builder.id(id))
                    .grants(grantList2)
                    .build();

            PutBucketAclRequest putAclReq = PutBucketAclRequest.builder()
                    .bucket(bucketName)
                    .accessControlPolicy(acl)
                    .build();

            s3Client.putBucketAcl(putAclReq);

        } catch (S3Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Deletes the CORS (Cross-Origin Resource Sharing) configuration for an Amazon S3 bucket.
     *
     * @param bucketName    the name of the Amazon S3 bucket for which the CORS configuration should be deleted
     * @param accountId     the expected AWS account ID of the bucket owner
     *
     * @throws S3Exception if an error occurs while deleting the CORS configuration for the bucket
     */
    @Override
    public void deleteBucketCorsInformation(String bucketName, String accountId) {
        try {
            DeleteBucketCorsRequest bucketCorsRequest = DeleteBucketCorsRequest.builder()
                    .bucket(bucketName)
                    .expectedBucketOwner(accountId)
                    .build();

            s3Client.deleteBucketCors(bucketCorsRequest);

        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    /**
     * Retrieves the CORS (Cross-Origin Resource Sharing) configuration for the specified S3 bucket.
     *
     * @param bucketName the name of the S3 bucket to retrieve the CORS configuration for
     * @param accountId the expected bucket owner's account ID
     *
     * @throws S3Exception if there is an error retrieving the CORS configuration
     */
    @Override
    public void getBucketCorsInformation(String bucketName, String accountId) {
        try {
            GetBucketCorsRequest bucketCorsRequest = GetBucketCorsRequest.builder()
                    .bucket(bucketName)
                    .expectedBucketOwner(accountId)
                    .build();

            GetBucketCorsResponse corsResponse = s3Client.getBucketCors(bucketCorsRequest);
            List<CORSRule> corsRules = corsResponse.corsRules();
            for (CORSRule rule : corsRules) {
                System.out.println("allowOrigins: " + rule.allowedOrigins());
                System.out.println("AllowedMethod: " + rule.allowedMethods());
            }

        } catch (S3Exception e) {

            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    /**
     * Sets the Cross-Origin Resource Sharing (CORS) rules for an Amazon S3 bucket.
     *
     * @param bucketName The name of the S3 bucket to set the CORS rules for.
     * @param accountId The AWS account ID of the bucket owner.
     */
    @Override
    public void setCorsInformation(String bucketName, String accountId) {
        List<String> allowMethods = new ArrayList<>();
        allowMethods.add("PUT");
        allowMethods.add("POST");
        allowMethods.add("DELETE");

        List<String> allowOrigins = new ArrayList<>();
        allowOrigins.add("http://example.com");
        try {
            // Define CORS rules.
            CORSRule corsRule = CORSRule.builder()
                    .allowedMethods(allowMethods)
                    .allowedOrigins(allowOrigins)
                    .build();

            List<CORSRule> corsRules = new ArrayList<>();
            corsRules.add(corsRule);
            CORSConfiguration configuration = CORSConfiguration.builder()
                    .corsRules(corsRules)
                    .build();

            PutBucketCorsRequest putBucketCorsRequest = PutBucketCorsRequest.builder()
                    .bucket(bucketName)
                    .corsConfiguration(configuration)
                    .expectedBucketOwner(accountId)
                    .build();

            s3Client.putBucketCors(putBucketCorsRequest);

        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    /**
     * Sets the lifecycle configuration for an Amazon S3 bucket.
     *
     * @param bucketName   The name of the Amazon S3 bucket.
     * @param accountId    The expected owner of the Amazon S3 bucket.
     *
     * @throws S3Exception if there is an error setting the lifecycle configuration.
     */
    @Override
    public void setLifecycleConfig(String bucketName, String accountId) {
        try {
            // Create a rule to archive objects with the "glacierobjects/" prefix to the
            // S3 Glacier Flexible Retrieval storage class immediately.
            LifecycleRuleFilter ruleFilter = LifecycleRuleFilter.builder()
                    .prefix("glacierobjects/")
                    .build();

            Transition transition = Transition.builder()
                    .storageClass(TransitionStorageClass.GLACIER)
                    .days(0)
                    .build();

            LifecycleRule rule1 = LifecycleRule.builder()
                    .id("Archive immediately rule")
                    .filter(ruleFilter)
                    .transitions(transition)
                    .status(ExpirationStatus.ENABLED)
                    .build();

            // Create a second rule.
            Transition transition2 = Transition.builder()
                    .storageClass(TransitionStorageClass.GLACIER)
                    .days(0)
                    .build();

            List<Transition> transitionList = new ArrayList<>();
            transitionList.add(transition2);

            LifecycleRuleFilter ruleFilter2 = LifecycleRuleFilter.builder()
                    .prefix("glacierobjects/")
                    .build();

            LifecycleRule rule2 = LifecycleRule.builder()
                    .id("Archive and then delete rule")
                    .filter(ruleFilter2)
                    .transitions(transitionList)
                    .status(ExpirationStatus.ENABLED)
                    .build();

            // Add the LifecycleRule objects to an ArrayList.
            ArrayList<LifecycleRule> ruleList = new ArrayList<>();
            ruleList.add(rule1);
            ruleList.add(rule2);

            BucketLifecycleConfiguration lifecycleConfiguration = BucketLifecycleConfiguration.builder()
                    .rules(ruleList)
                    .build();

            PutBucketLifecycleConfigurationRequest putBucketLifecycleConfigurationRequest = PutBucketLifecycleConfigurationRequest
                    .builder()
                    .bucket(bucketName)
                    .lifecycleConfiguration(lifecycleConfiguration)
                    .expectedBucketOwner(accountId)
                    .build();

            s3Client.putBucketLifecycleConfiguration(putBucketLifecycleConfigurationRequest);

        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    /**
     * Retrieves the lifecycle configuration for an Amazon S3 bucket and adds a new lifecycle rule to it.
     *
     * @param bucketName the name of the Amazon S3 bucket
     * @param accountId the expected owner of the Amazon S3 bucket
     */
    @Override
    public void getLifecycleConfig(String bucketName, String accountId){
        try {
            GetBucketLifecycleConfigurationRequest getBucketLifecycleConfigurationRequest = GetBucketLifecycleConfigurationRequest
                    .builder()
                    .bucket(bucketName)
                    .expectedBucketOwner(accountId)
                    .build();

            GetBucketLifecycleConfigurationResponse response = s3Client.getBucketLifecycleConfiguration(getBucketLifecycleConfigurationRequest);
            List<LifecycleRule> newList = new ArrayList<>();
            List<LifecycleRule> rules = response.rules();
            for (LifecycleRule rule : rules) {
                newList.add(rule);
            }

            // Add a new rule with both a prefix predicate and a tag predicate.
            LifecycleRuleFilter ruleFilter = LifecycleRuleFilter.builder()
                    .prefix("YearlyDocuments/")
                    .build();

            Transition transition = Transition.builder()
                    .storageClass(TransitionStorageClass.GLACIER)
                    .days(3650)
                    .build();

            LifecycleRule rule1 = LifecycleRule.builder()
                    .id("NewRule")
                    .filter(ruleFilter)
                    .transitions(transition)
                    .status(ExpirationStatus.ENABLED)
                    .build();

            // Add the new rule to the list.
            newList.add(rule1);
            BucketLifecycleConfiguration lifecycleConfiguration = BucketLifecycleConfiguration.builder()
                    .rules(newList)
                    .build();

            PutBucketLifecycleConfigurationRequest putBucketLifecycleConfigurationRequest = PutBucketLifecycleConfigurationRequest
                    .builder()
                    .bucket(bucketName)
                    .lifecycleConfiguration(lifecycleConfiguration)
                    .expectedBucketOwner(accountId)
                    .build();

            s3Client.putBucketLifecycleConfiguration(putBucketLifecycleConfigurationRequest);

        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    /**
     * Deletes the lifecycle configuration for an Amazon S3 bucket.
     *
     * @param bucketName the name of the S3 bucket
     * @param accountId the expected account owner of the S3 bucket
     *
     * @throws S3Exception if an error occurs while deleting the lifecycle configuration
     */
    @Override
    public void deleteLifecycleConfig(String bucketName, String accountId){
        try {
            DeleteBucketLifecycleRequest deleteBucketLifecycleRequest = DeleteBucketLifecycleRequest
                    .builder()
                    .bucket(bucketName)
                    .expectedBucketOwner(accountId)
                    .build();

            s3Client.deleteBucketLifecycle(deleteBucketLifecycleRequest);

        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    /**
     * Sets the policy for an Amazon S3 bucket.
     *
     * @param bucketName the name of the Amazon S3 bucket
     * @param policyText the text of the policy to be set on the bucket
     * @throws S3Exception if there is an error setting the bucket policy
     */
    @Override
    public void setPolicy(String bucketName, String policyText) {
        System.out.println("Setting policy:");
        System.out.println("----");
        System.out.println(policyText);
        System.out.println("----");
        System.out.format("On Amazon S3 bucket: \"%s\"\n", bucketName);

        try {
            PutBucketPolicyRequest policyReq = PutBucketPolicyRequest.builder()
                    .bucket(bucketName)
                    .policy(policyText)
                    .build();

            s3Client.putBucketPolicy(policyReq);

        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }

        System.out.println("Done!");
    }

    /**
     * Retrieves the bucket policy from a specified file.
     *
     * @param policyFile the path to the file containing the bucket policy
     * @return the content of the bucket policy file as a string
     */
    @Override
    public String getBucketPolicyFromFile(String policyFile) {
        StringBuilder fileText = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(Paths.get(policyFile), StandardCharsets.UTF_8);
            for (String line : lines) {
                fileText.append(line);
            }

        } catch (IOException e) {
            System.out.format("Problem reading file: \"%s\"", policyFile);
            System.out.println(e.getMessage());
        }

        try {
            final JsonParser parser = new ObjectMapper().getFactory().createParser(fileText.toString());
            while (parser.nextToken() != null) {
            }

        } catch (IOException jpe) {
            jpe.printStackTrace();
        }
        return fileText.toString();
    }

    /**
     * Performs a multipart upload to Amazon S3 using the provided S3 client.
     *
     * @param filePath the path to the file to be uploaded
     */
    @Override
    public void multipartUploadWithS3Client(String bucketName, String key, String filePath) {

        // Initiate the multipart upload.
        CreateMultipartUploadResponse createMultipartUploadResponse = s3Client.createMultipartUpload(b -> b
                .bucket(bucketName)
                .key(key));
        String uploadId = createMultipartUploadResponse.uploadId();

        // Upload the parts of the file.
        int partNumber = 1;
        List<CompletedPart> completedParts = new ArrayList<>();
        ByteBuffer bb = ByteBuffer.allocate(1024 * 1024 * 5); // 5 MB byte buffer

        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            long fileSize = file.length();
            long position = 0;
            while (position < fileSize) {
                file.seek(position);
                long read = file.getChannel().read(bb);

                bb.flip(); // Swap position and limit before reading from the buffer.
                UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .uploadId(uploadId)
                        .partNumber(partNumber)
                        .build();

                UploadPartResponse partResponse = s3Client.uploadPart(
                        uploadPartRequest,
                        RequestBody.fromByteBuffer(bb));

                CompletedPart part = CompletedPart.builder()
                        .partNumber(partNumber)
                        .eTag(partResponse.eTag())
                        .build();
                completedParts.add(part);

                bb.clear();
                position += read;
                partNumber++;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        // Complete the multipart upload.
        s3Client.completeMultipartUpload(b -> b
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .multipartUpload(CompletedMultipartUpload.builder().parts(completedParts).build()));
    }

}

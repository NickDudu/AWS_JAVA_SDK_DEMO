package com.nick.sdk.s3;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GetTheLatestFileByDay {
    public static void main(String[] args) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime now = LocalDate.now().atStartOfDay();
        Instant instant = now.toInstant(ZoneOffset.UTC);
        LocalDate localDate = LocalDate.parse("2020-07-17");
        LocalDateTime localDateTime = localDate.atStartOfDay();

        //String bucketName = args[0];
        //String keyName = args[1];
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create("cn");
        Region region = Region.CN_NORTH_1;
        S3Presigner presigner = S3Presigner.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();

        S3Client s3 = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();

        ListObjectsRequest listObjects = ListObjectsRequest
                .builder()
                .bucket("testnickhu")
                .build();

        ListObjectsResponse res = s3.listObjects(listObjects);
        List<S3Object> objects = res.contents();
        for (S3Object myValue : objects) {
            if (myValue.lastModified().isAfter(instant) == true) {
                System.out.print("\n" + myValue.key());
                //System.out.print("\n" + instant);
                String objectKey = myValue.key();
                String fromBucket = "testnickhu";
                String toBucket = "nickbilling";
                System.out.format("Copying object %s from bucket %s to %s\n", objectKey, fromBucket, toBucket);
                copyBucketObject (s3, fromBucket, objectKey, toBucket);
            } else {
                //not right
            }

        }

        s3.close();
    }

    public static String copyBucketObject (S3Client s3, String fromBucket, String objectKey, String toBucket) {

        String encodedUrl = "";
        try {
            encodedUrl = URLEncoder.encode(fromBucket + "/" + objectKey, StandardCharsets.UTF_8.toString());

        } catch (UnsupportedEncodingException e) {
            System.out.println("URL could not be encoded: " + e.getMessage());
        }

        CopyObjectRequest copyReq = CopyObjectRequest.builder()
                .sourceBucket(fromBucket)
                .sourceKey(objectKey)
                .destinationBucket(toBucket)
                .destinationKey(objectKey)
                .build();

        try {
            CopyObjectResponse copyRes = s3.copyObject(copyReq);
            return copyRes.copyObjectResult().toString();

        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return "";
    }

    public static void createBucket( S3Client s3Client, String bucketName) {

        try {
            S3Waiter s3Waiter = s3Client.waiter();
            CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.createBucket(bucketRequest);
            HeadBucketRequest bucketRequestWait = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            // Wait until the bucket is created and print out the response.
            WaiterResponse<HeadBucketResponse> waiterResponse = s3Waiter.waitUntilBucketExists(bucketRequestWait);
            waiterResponse.matched().response().ifPresent(System.out::println);
            System.out.println(bucketName +" is ready");

        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }
}

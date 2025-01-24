package com.example.pingmate;

import android.content.Context;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;

public class S3ClientManager {

    private static AmazonS3Client s3Client;
    private static TransferUtility transferUtility;

    public static void initialize(Context context, String accessKey, String secretKey, String region) {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        s3Client = new AmazonS3Client(credentials);
        s3Client.setRegion(com.amazonaws.regions.Region.getRegion(region));
        transferUtility = TransferUtility.builder().s3Client(s3Client).context(context).build();
    }

    public static TransferUtility getTransferUtility() {
        return transferUtility;
    }
}

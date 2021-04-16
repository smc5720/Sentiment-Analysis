package com.example.googlestt_0;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;

public class AWSService {
    private static final String BUCKET_NAME = "noding";
    private static final String ACCESS_KEY = "AKIAWZAUUDMXMBW2ULYX";
    private static final String SECRET_KEY = "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";
    private AmazonS3 amazonS3;

    public AWSService() {
        AWSCredentials awsCredentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
        amazonS3 = new AmazonS3Client(awsCredentials);
    }

    public void uploadFile(File file) {
        if (amazonS3 != null) {
            try {
                PutObjectRequest putObjectRequest =
                        new PutObjectRequest(BUCKET_NAME + "text_data"/*sub directory*/, file.getName(), file);
                putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead); // file permission
                amazonS3.putObject(putObjectRequest); // upload file

            } catch (AmazonServiceException ase) {
                ase.printStackTrace();
            } finally {
                amazonS3 = null;
            }
        }
    }


}


//출처: https://dwfox.tistory.com/57 [DWFOX]
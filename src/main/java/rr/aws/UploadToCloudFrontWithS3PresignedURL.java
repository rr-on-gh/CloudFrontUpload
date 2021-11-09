package rr.aws;

import org.apache.http.client.utils.URIBuilder;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;

public class UploadToCloudFrontWithS3PresignedURL {

    public static void main(String[] args) throws IOException, URISyntaxException {
        String keyName = "test_file.txt";
        String bucketName = "";
        String region = "us-east-1";
        String cloudFrontDistribution = "";

        // S3Presigner may create the URL with hostname without specifying the bucket region.
        // For the S3 presigned URL to work with CloudFront the S3 bucket region must be explicitly set
        S3Presigner presigner = S3Presigner.builder()
                .credentialsProvider(ProfileCredentialsProvider.builder().profileName("first").build())
                .endpointOverride(new URI("https://s3." + region + ".amazonaws.com"))
                .build();
        putToCloudFrontUsingS3PresignedURL(presigner, bucketName, keyName, cloudFrontDistribution);
    }

    private static URL createS3PresignedUrl(S3Presigner presigner, String bucketName, String keyName) {

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .contentType("text/plain")
                .build();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60))
                .putObjectRequest(objectRequest)
                .build();
        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

        String myURL = presignedRequest.url().toString();
        System.out.println("S3 Presigned URL: \n" + myURL);

        return presignedRequest.url();
    }

    private static void putToCloudFrontUsingS3PresignedURL(S3Presigner presigner, String bucketName, String keyName, String cloudFrontDistribution)
            throws URISyntaxException, IOException {
        URL url = UploadToCloudFrontWithS3PresignedURL.createS3PresignedUrl(presigner, bucketName, keyName);

        //Change to cloudfront URL
        URL cloudFrontUrl = new URIBuilder(URI.create(url.toString()))
                .setHost(cloudFrontDistribution)
                .build().toURL();

        System.out.println("CF URL:\n" + cloudFrontUrl);
        int responseCode = uploadObject(cloudFrontUrl);
        System.out.println("HTTP response code is " + responseCode);
    }

    private static int uploadObject(URL cloudFrontUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) cloudFrontUrl.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "text/plain");
        connection.setRequestMethod("PUT");
        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
        out.write("This file was uploaded as an object by using a S3 presigned URL from CloudFront.");
        out.close();
        return connection.getResponseCode();
    }
}

# Upload to S3 through CloudFront using S3 presigned URLs

You can use CloudFront to accelerate uploads to S3. AWS also offers S3 Transfer Acceleration (S3TA) to speed up uploads. In fact, S3TA might be a better choice to upload to S3 instead of CloudFront. 
However, if for some specific reasons you do choose to use CloudFront to accelerate your uploads, the following steps describe how this can be done using S3 presigned URLs

## Step 1: Create an Origin Request Policy
In the AWS console, navigate to CloudFront -> Policies -> Origin request -> Create origin request policy
Create the origin request policy with [these settings](https://raw.githubusercontent.com/rr-on-gh/CloudFrontUpload/master/Origin_Request_Policy.png) that forwards all query strings to the origin which in this case will be S3.

## Step 2: Create the CloudFront distribution
Create a new CloudFront distribution with [these settings](https://raw.githubusercontent.com/rr-on-gh/CloudFrontUpload/master/CloudFront_Creation.png). Make sure to select the Origin Request policy you created in the previous step. 

## Step 3: Generate the presigned URL for upload
Once the CloudFront distribution is deployed you can now use S3 presigned URLs to upload objects to S3. You can generate the S3 presigned URLs the way you used to before. Once generated, you just need to replace the host part of the url with you CloudFront distribution's domain.

For eg, if S3 presigned URL is 
```
https://S3BUCKETNAME.s3.us-east-1.amazonaws.com/test_file.txt?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20211109T105053Z&X-Amz-SignedHeaders=content-type%3Bhost&X-Amz-Expires=3600&X-Amz-Credential=XXXXX%2F20211109%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=yyyy
```
the CloudFront URL would be:
```
https://dxxxxxxxx.cloudfront.net/test_file.txt?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20211109T105053Z&X-Amz-SignedHeaders=content-type%3Bhost&X-Amz-Expires=3600&X-Amz-Credential=XXXXX%2F20211109%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=yyyy
```
Refer [`UploadThroughCloudFrontWithS3PresignedURL`](https://github.com/rr-on-gh/CloudFrontUpload/blob/master/src/main/java/rr/aws/UploadThroughCloudFrontWithS3PresignedURL.java#L20) for example of generation of the presigned URL and the nits involved while replacing the hostname with the CloudFront distribution domain name. 
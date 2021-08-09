package ua.edu.ua.cloud.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class SQSProducerHandler implements RequestHandler<S3Event, Void> {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String QUEUE_NAME = "reddit-queue.fifo";
    private AmazonS3 s3Client;

    @Override
    public Void handleRequest(S3Event event, Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            // log execution details
            logEnvironment(event, context);

            s3Client = AmazonS3ClientBuilder.defaultClient();

            for (S3EventNotification.S3EventNotificationRecord record : event.getRecords()) {

                String srcBucket = record.getS3().getBucket().getName();

                // Object key may have spaces or unicode non-ASCII characters.
                String srcKey = record.getS3().getObject().getUrlDecodedKey();

                // Download the file from S3 into a stream
                S3Object s3Object = s3Client.getObject(new GetObjectRequest(srcBucket, srcKey));
                InputStream inputFile = s3Object.getObjectContent();

                writeToSQS(inputFile, context);

                logger.log(String.format("Read complete: delete file {} from S3 bucket {}", srcBucket, srcKey));
                s3Client.deleteObject(srcBucket, srcKey);
            }
        } catch (Throwable ex) {
            logger.log(String.format("Error: ", ex.getLocalizedMessage()));
            throw ex;
        }

        return null;
    }

    private void writeToSQS(InputStream inputFile, Context context) {
        int recordNbr = 0;
        Instant startedAt = Instant.now();
        InputStreamReader inputStreamReader = new InputStreamReader(inputFile);

        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
        String queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();

        try (BufferedReader br = new BufferedReader(inputStreamReader)) {
            String line = br.readLine(); // skip header

            while ((line = br.readLine()) != null) {
                recordNbr++;
                line = Instant.now() + "," + line;

                Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
                messageAttributes.put("AttributeOne", new MessageAttributeValue()
                        .withStringValue("This is an attribute")
                        .withDataType("String"));

                SendMessageRequest sendMessageRequest = new SendMessageRequest()
                        .withQueueUrl(queueUrl)
                        .withMessageBody(line)
                        .withMessageGroupId("reddit")
                        .withMessageAttributes(messageAttributes);
                sqs.sendMessage(sendMessageRequest);
            }
        } catch (FileNotFoundException e) {
            context.getLogger().log(e.getLocalizedMessage());
        } catch (IOException e) {
            context.getLogger().log(e.getLocalizedMessage());
        }

        context.getLogger().log(
                recordNbr + " messages were produced to queue '" + QUEUE_NAME + "' and took " + Duration
                        .between(startedAt, Instant.now()));
    }

    public static void logEnvironment(Object event, Context context) {
        LambdaLogger logger = context.getLogger();
        // log execution details
        logger.log("ENVIRONMENT VARIABLES: " + gson.toJson(System.getenv()));
        logger.log("CONTEXT: " + gson.toJson(context));
        // log event details
        logger.log("EVENT: " + gson.toJson(event));
        logger.log("EVENT TYPE: " + event.getClass().toString());
    }

}
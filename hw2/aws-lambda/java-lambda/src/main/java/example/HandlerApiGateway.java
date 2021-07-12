package example;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.StringInputStream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class HandlerApiGateway implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String S3_BUCKET = "lambda-java-http-requests";

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        // Function could be triggered via HTTP request
        // Function does not allow anonymous access

        // log execution details
        logEnvironment(event, context);

        // Content of a request is stored as json file in Object Store
        String json = event.toString(); // returns json
        String fileName = "request_" + context.getAwsRequestId() + ".json";

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(json.length());
        meta.setContentType("json");

        try {
            // Uploading to S3 destination bucket
            context.getLogger().log("Writing to: " + S3_BUCKET);
            AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
            s3Client.putObject(S3_BUCKET, fileName, new StringInputStream(json), meta);
        } catch (AmazonServiceException | UnsupportedEncodingException e) {
            context.getLogger().log(e.getLocalizedMessage());
            return getResponse(500, e.getLocalizedMessage());
        }

        return getResponse(200, String.format("File %s is created in bucket %s", fileName, S3_BUCKET));
    }

    private APIGatewayProxyResponseEvent getResponse(Integer statusCode, String body) {

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "plain/text");
        response.setHeaders(headers);
        response.setIsBase64Encoded(false);
        response.setStatusCode(statusCode);
        response.setBody(body);

        return response;
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
package ua.edu.ua.cloud.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SQSConsumerHandler implements RequestHandler<SQSEvent, Void> {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Void handleRequest(SQSEvent event, Context context) {
        try {
            // log execution details
            logEnvironment(event, context);

            for (SQSEvent.SQSMessage msg : event.getRecords()) {
                context.getLogger().log(msg.getBody());
            }
        } catch (Throwable ex) {
            context.getLogger().log(ex.getLocalizedMessage());
            throw ex;
        }

        return null;
    }

    public static void logEnvironment(Object event, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("EVENT: " + gson.toJson(event));
        logger.log("EVENT TYPE: " + event.getClass().toString());
    }
}
package ua.edu.ua.cloud.aws.lambda;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.PutMetricDataResult;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class SQSConsumerHandler implements RequestHandler<SQSEvent, Void> {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Void handleRequest(SQSEvent event, Context context) {
        try {
            // log execution details
            logEnvironment(event, context);

            for (SQSEvent.SQSMessage msg : event.getRecords()) {
                String body = msg.getBody();
                context.getLogger().log(body);

                long messageLatency = getMessageLatency(body);
                int wordCount = getWordCount(body);

                final AmazonCloudWatch cloudWatch =
                        AmazonCloudWatchClientBuilder.defaultClient();

                sendLatencyMetric(messageLatency, cloudWatch);
                sendWordCountMetric(wordCount, cloudWatch);
            }
        } catch (Throwable ex) {
            context.getLogger().log(ex.getLocalizedMessage());
            throw ex;
        }

        return null;
    }

    private void sendLatencyMetric(long messageLatency, AmazonCloudWatch cloudWatch) {
        Dimension dimension = new Dimension()
                .withName("LATENCY")
                .withValue("MILLISECONDS");

        MetricDatum datum = new MetricDatum()
                .withMetricName("LATENCY")
                .withUnit(StandardUnit.Milliseconds)
                .withValue(new Double(messageLatency))
                .withDimensions(dimension);

        PutMetricDataRequest request = new PutMetricDataRequest()
                .withNamespace("REDDIT/SQS_LATENCY")
                .withMetricData(datum);

        cloudWatch.putMetricData(request);
    }

    private void sendWordCountMetric(long wordCount, AmazonCloudWatch cloudWatch) {
        Dimension dimension = new Dimension()
                .withName("WORD")
                .withValue("COUNT");

        MetricDatum datum = new MetricDatum()
                .withMetricName("WORD_COUNT")
                .withUnit(StandardUnit.Count)
                .withValue(new Double(wordCount))
                .withDimensions(dimension);

        PutMetricDataRequest request = new PutMetricDataRequest()
                .withNamespace("REDDIT/WORDS")
                .withMetricData(datum);

        cloudWatch.putMetricData(request);
    }

    private long getMessageLatency(String body) {
        try {
            Instant producer_time = Instant.parse(body.substring(0, body.indexOf(",")));
            Instant consumer_time = Instant.now();
            return ChronoUnit.MILLIS.between(producer_time, consumer_time);
        } catch (DateTimeParseException ex) {
            return -1L;
        }
    }

    public void logEnvironment(Object event, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("EVENT: " + gson.toJson(event));
        logger.log("EVENT TYPE: " + event.getClass().toString());
    }

    public int getWordCount(String message) {
        if (StringUtils.isNullOrEmpty(message)) {
            return 0;
        }

        String[] wordArray = message.trim().split("\\s+");
        return wordArray.length;
    }
}
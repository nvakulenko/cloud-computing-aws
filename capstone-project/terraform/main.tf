provider "aws" {
  region = var.region
}

resource "aws_sqs_queue" "sqs_queue" {
  name                        = "reddit-queue.fifo"
  fifo_queue                  = true
  content_based_deduplication = true
  visibility_timeout_seconds  = 60
}

//// Create a log group for the lambda
//resource "aws_cloudwatch_log_group" "log_group" {
//  name = "/aws/lambda/reddit_sqs"
////  /aws/lambda/java_producer_lambda_function'
//}

# allow lambda to log to cloudwatch
data "aws_iam_policy_document" "cloudwatch_log_group_access_document" {
  statement {
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]

    resources = [
      "arn:aws:logs:::*",
    ]
  }
}

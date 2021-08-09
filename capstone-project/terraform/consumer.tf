resource "aws_lambda_function" "java_consumer_lambda_function" {
  runtime          = var.lambda_runtime
  filename         = var.consumer_lambda_payload_filename
  source_code_hash = filebase64sha256(var.consumer_lambda_payload_filename)
  function_name    = "java_consumer_lambda_function"

  handler          = var.consumer_lambda_function_handler
  timeout          = 60
  memory_size      = 256
  role             = aws_iam_role.iam_role_for_consumer_lambda.arn
  depends_on       = [//aws_cloudwatch_log_group.log_group
    aws_iam_role_policy_attachment.aws_iam_role_consumer_policy_attachment
  ]
}

# consumer lambda role
resource "aws_iam_role" "iam_role_for_consumer_lambda" {
  name = "lambda-invoke-role"
  assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
      {
        "Action": "sts:AssumeRole",
        "Principal": {
          "Service": "lambda.amazonaws.com"
        },
        "Effect": "Allow",
        "Sid": ""
      }
    ]
}
EOF
}

# lambda policy
resource "aws_iam_policy" "iam_policy_for_consumer_lambda" {
  name = "consumer-lambda-invoke-policy"
  path = "/"

  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
      {
        "Sid": "LambdaPolicy",
        "Effect": "Allow",
        "Action": [
          "cloudwatch:PutMetricData",
          "sqs:*",
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ],
        "Resource": "*"
      }
    ]
  }
EOF
}

# Attach the policy to the role
resource "aws_iam_role_policy_attachment" "aws_iam_role_consumer_policy_attachment" {
  role       = aws_iam_role.iam_role_for_consumer_lambda.name
  policy_arn = aws_iam_policy.iam_policy_for_consumer_lambda.arn
}

resource "aws_lambda_event_source_mapping" "sqs_consumer_mapping" {
  event_source_arn = aws_sqs_queue.sqs_queue.arn
  function_name    = aws_lambda_function.java_consumer_lambda_function.arn
}

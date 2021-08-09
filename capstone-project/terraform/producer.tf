resource "aws_lambda_function" "java_producer_lambda_function" {
  runtime          = var.lambda_runtime
  filename         = var.producer_lambda_payload_filename
  source_code_hash = filebase64sha256(var.producer_lambda_payload_filename)
  function_name    = "java_producer_lambda_function"

  handler          = var.producer_lambda_function_handler
  timeout          = 60
  memory_size      = 256
  role             = aws_iam_role.iam_role_for_producer_lambda.arn
  depends_on       = [
    //aws_cloudwatch_log_group.log_group,
    aws_iam_role_policy_attachment.aws_iam_role_producer_policy_attachment
  ]
}

# producer lambda role
resource "aws_iam_role" "iam_role_for_producer_lambda" {
  name = "producer-lambda-invoke-role"
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
resource "aws_iam_policy" "iam_policy_for_producer_lambda" {
  name = "lambda-invoke-policy"
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
          "s3:GetObject",
          "s3:DeleteObject",
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
resource "aws_iam_role_policy_attachment" "aws_iam_role_producer_policy_attachment" {
  role       = aws_iam_role.iam_role_for_producer_lambda.name
  policy_arn = aws_iam_policy.iam_policy_for_producer_lambda.arn
}

resource "aws_lambda_permission" "allow_bucket" {
  statement_id  = "AllowExecutionFromS3Bucket"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.java_producer_lambda_function.arn
  principal     = "s3.amazonaws.com"
  source_arn    = aws_s3_bucket.reddit-messages-bucket.arn
}

resource "aws_s3_bucket" "reddit-messages-bucket" {
  bucket = "reddit-messages"
  acl    = "private"
  force_destroy = true

  lifecycle {
    prevent_destroy = false
  }

  tags = {
    Name        = "Reddit messages source"
    Environment = "Dev"
  }
}

resource "aws_s3_bucket_notification" "bucket_notification" {
  bucket = aws_s3_bucket.reddit-messages-bucket.id

  lambda_function {
    lambda_function_arn = aws_lambda_function.java_producer_lambda_function.arn
    events              = ["s3:ObjectCreated:*"]
  }

  depends_on = [aws_lambda_permission.allow_bucket]
}
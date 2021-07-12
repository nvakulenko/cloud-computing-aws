resource "aws_s3_bucket" "lambda-java-http-requests-bucket" {
  bucket = "lambda-java-http-requests"
  acl    = "private"
  force_destroy = true

  lifecycle {
    prevent_destroy = false
  }

  tags = {
    Name        = "Lambda java HTTP requests"
    Environment = "Dev"
  }
}
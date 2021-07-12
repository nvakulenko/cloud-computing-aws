variable "region" {
  default = "eu-central-1"
}

variable "lambda_payload_filename" {
  default = "../java-lambda/target/java-lambda-1.0-SNAPSHOT.jar"
}

variable "lambda_function_handler" {
  default = "example.HandlerApiGateway"
}

variable "lambda_runtime" {
  default = "java8"
}

variable "api_path" {
  default = "{proxy+}"
}

variable "api_env_stage_name" {
  default = "lambda-java-stage"
}

variable "region" {
  default = "eu-central-1"
}

variable "producer_lambda_payload_filename" {
  default = "./producer/target/producer-0.0.1-SNAPSHOT.jar"
}

variable "consumer_lambda_payload_filename" {
  default = "./consumer/target/consumer-0.0.1-SNAPSHOT.jar"
}

variable "producer_lambda_function_handler" {
  default = "ua.edu.ua.cloud.aws.lambda.SQSProducerHandler"
}

variable "consumer_lambda_function_handler" {
  default = "ua.edu.ua.cloud.aws.lambda.SQSConsumerHandler"
}

variable "lambda_runtime" {
  default = "java8"
}

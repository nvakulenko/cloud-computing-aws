locals {
  owner = "natasha.vakulenko@gmail.com"
  stack = "lambda-java"
  name = "lambda-java"
}

# terraform modules
module "java_lambda" {
  source = "./terraform/"
}

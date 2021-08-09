# Cloud Computing capstone project

## Team 
Nataliia Vakulenko

## Cloud
AWS

## Idea
The main idea of the project is to get experience with Terraform deployment to AWS. 
For that purpose I'm going to take an existing project from data streaming course and to deploy it to AWS using Terraform.

It will be a simple reddit messages word count analyzer. The biggest complexity is to use Terraform for deployment.

## High-level design

### Diagram
![Diagram](https://raw.githubusercontent.com/nvakulenko/cloud-computing-aws/main/capstone-project/Design.png)

### Components
- S3 Bucket - storage for files which contain reddit comments
- Producer - is triggered by events from S3 bucket - if a new file appears in the directory, then Producer reads it, parses to records and sends to SQS. It is a simple java-lambda.
- SQS - Amazon Simple Queue Service (SQS) is a fully managed message queuing service.
- Consumer - reads messages from SQS queue and performs very simple text analysis - keywords count. It is a simple java-lambda.
Consumer gather some statistics and send it to CloudWatch Metrics:
- message latency - delivery time for a massage 
- word count in a message

#### Used AWS Components:
- CloudWatch:
  - logs
  - metrics
- S3
- SQS
- Lambda Functions
- Triggers:
  - S3 Bucket Event trigger
  - SQS Event trigger
  
#### Build and deploy application 
Build consumer application:

`cd /consumer`

`mvn clean install`

`cd ..`

Build producer application:

`cd /producer`

`mvn clean install`

`cd ..`

Deploy terraform:

`terraform init`

`terraform deploy`
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
- Producer - listens to events from S3 bucket, if a new file appears in the directory, then Producer reads it, parses to records and sends to Kafka broker. Can be simple javaserver application or java-lambda.
- Kafka broker
- Consumer - reads messages from the topic and performs very simple text analysis - keywords count
Consumer can also gather some statistics and send it to CloudWatch:
- message latency - delivery time for a massage 
- average word count in message

#### AWS Components:
- CloudWatch
- S3
- Amazon msk 
- ...

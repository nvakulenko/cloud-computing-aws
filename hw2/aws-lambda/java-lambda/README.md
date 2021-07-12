# First Java HTTP function on AWS, deployed with Terraform

The project source includes function code and supporting resources:

- `src/main` - A Java function.
- `pom.xml` - A Maven build file.
- `./terraform` - Terraform files

Use the following instructions to deploy the sample application.

    mvn clean install
    terraform init
    terraform plan
    terraform apply

Destroy infrastructure after testing
    
    terraform destroy

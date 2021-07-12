resource "aws_iam_user" "demo-user" {
  name = "demo-user"
  path = "/"
  force_destroy = true

  tags = {
    tag-key = "tag-value"
  }
}

resource "aws_iam_access_key" "demo-user" {
  user = aws_iam_user.demo-user.name
}

resource "aws_iam_user_policy" "demo-user-role" {
  name = "test"
  user = aws_iam_user.demo-user.name

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": [
        "execute-api:Invoke",
        "execute-api:ManageConnections"
      ],
      "Effect": "Allow",
      "Resource": "arn:aws:execute-api:*:*:*"
    }
  ]
}
EOF
}
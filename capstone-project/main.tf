#locals {
#  stack = "reddit-messages"
#  name = "reddit-messages"
#}

# terraform modules
module "reddit_messages" {
  source = "./terraform/"
}

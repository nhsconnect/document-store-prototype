data "aws_cloudformation_export" "proactive_notifications_sns_topic" {
  name = "${aws_cloudformation_stack.s3_virus_scanning_stack.name}-proactive-notifications-sns-topic"
}

resource "aws_sns_topic_subscription" "proactive_notifications_sns_topic_subscription" {
  endpoint = data.aws_ssm_parameter.cloud_security_email.value
  protocol = "email"
  topic_arn = data.aws_cloudformation_export.proactive_notifications_sns_topic.value
}
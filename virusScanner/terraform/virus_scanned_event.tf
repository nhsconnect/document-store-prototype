resource "aws_iam_role" "virus_scan_lambda_role" {
  name               = "VirusScanEventExecution"
  assume_role_policy = jsonencode({
    Version   = "2012-10-17"
    Statement = [
      {
        Action    = "sts:AssumeRole"
        Effect    = "Allow"
        Sid       = ""
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_lambda_function" "virus_scanned_event_lambda" {
  function_name    = "VirusScannedEventHandler"
  role             = aws_iam_role.virus_scan_lambda_role.arn
  handler          = "uk.nhs.digital.virusScanner.VirusScannedEventHandler::handleRequest"
  runtime          = "java11"
  filename         = var.lambda_jar_filename
  source_code_hash = filebase64sha256(var.lambda_jar_filename)
  timeout = 15
  memory_size = 256
  layers = [
    "arn:aws:lambda:eu-west-2:580247275435:layer:LambdaInsightsExtension:21"
  ]
}
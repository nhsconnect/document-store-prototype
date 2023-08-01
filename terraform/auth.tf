resource "aws_api_gateway_resource" "auth_resource" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  parent_id   = aws_api_gateway_rest_api.lambda_api.root_resource_id
  path_part   = "Auth"
}

resource "aws_dynamodb_table" "access_request_fulfilment_auth" {
  name           = "${terraform.workspace}_ARFAuth"
  hash_key       = "PK"
  range_key      = "SK"
  billing_mode   = "PAY_PER_REQUEST"
  stream_enabled = false

  ttl {
    attribute_name = "TimeToExist"
    enabled        = true
  }

  attribute {
    name = "PK"
    type = "S"
  }

  attribute {
    name = "SK"
    type = "S"
  }
}

resource "aws_iam_policy" "arf_auth_table_policy" {
  name = "${terraform.workspace}_arf_auth_table_policy"
  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "dynamodb:Query",
          "dynamodb:Scan"
        ],
        "Resource" : [
          aws_dynamodb_table.access_request_fulfilment_auth.arn
        ]
      },
      {
        "Effect" : "Allow",
        "Action" : [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:UpdateItem",
          "dynamodb:DeleteItem",
        ],
        "Resource" : [
          aws_dynamodb_table.access_request_fulfilment_auth.arn
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "auth_role_auth_table_access" {
  policy_arn = aws_iam_policy.arf_auth_table_policy.arn
  role       = aws_iam_role.authoriser_execution_role.name
}

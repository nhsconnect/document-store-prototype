resource "aws_dynamodb_table" "doc_ref_store" {
  name           = "DocumentReferenceMetadata"
  hash_key       = "ID"
  billing_mode   = "PAY_PER_REQUEST"
  stream_enabled = false

  attribute {
    name = "ID"
    type = "S"
  }

  attribute {
    name = "Location"
    type = "S"
  }

  attribute {
    name = "NhsNumber"
    type = "S"
  }

  global_secondary_index {
    name            = "LocationsIndex"
    hash_key        = "Location"
    projection_type = "ALL"
  }

  global_secondary_index {
    name            = "NhsNumberIndex"
    hash_key        = "NhsNumber"
    projection_type = "ALL"
  }
}

resource "aws_dynamodb_table" "doc_zip_trace_store" {
  name           = "DocumentZipTrace"
  hash_key       = "ID"
  billing_mode   = "PAY_PER_REQUEST"
  stream_enabled = false

  attribute {
    name = "ID"
    type = "S"
  }

  ttl {
    attribute_name = "ExpiryDate"
    enabled        = true
  }
}

resource "aws_iam_role_policy" "dynamodb_query_locations_policy" {
  name = "dynamodb_query_locations_policy"
  role = aws_iam_role.lambda_execution_role.id

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "dynamodb:Scan",
          "dynamodb:Query"
        ],
        "Resource" : [
          aws_dynamodb_table.doc_ref_store.arn
        ]
      },
      {
        "Effect" : "Allow",
        "Action" : [
          "dynamodb:Query",
        ],
        "Resource" : [
          "${aws_dynamodb_table.doc_ref_store.arn}/index/NhsNumberIndex"
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy" "dynamodb_get_document_reference_policy" {
  name = "get_document_reference_policy"
  role = aws_iam_role.lambda_execution_role.id

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:UpdateItem",
        ],
        "Resource" : [
          aws_dynamodb_table.doc_ref_store.arn,
          aws_dynamodb_table.doc_zip_trace_store.arn,
        ]
      }
    ]
  })
}


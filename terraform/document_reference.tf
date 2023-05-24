module "doc_ref_collection_preflight" {
  source         = "./modules/api_gateway_preflight"
  api_gateway_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id    = aws_api_gateway_resource.doc_ref_collection_resource.id
  origin         = var.cloud_only_service_instances > 0 ? "'https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com'" : "'*'"
  methods        = "'GET,OPTIONS,POST,DELETE'"
}

resource "aws_api_gateway_resource" "doc_ref_collection_resource" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  parent_id   = aws_api_gateway_rest_api.lambda_api.root_resource_id
  path_part   = "DocumentReference"
}

resource "aws_dynamodb_table" "doc_ref_store" {
  name           = "${terraform.workspace}_DocumentReferenceMetadata"
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

resource "aws_iam_role_policy" "arf_doc_store_data_access_policy" {
  name = "doc_store_data_access_policy"
  role = aws_iam_role.lambda_execution_role.id

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "dynamodb:Scan",
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
          "${aws_dynamodb_table.doc_ref_store.arn}/index/NhsNumberIndex",
          "${aws_dynamodb_table.doc_ref_store.arn}/index/LocationsIndex"
        ]
      },
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

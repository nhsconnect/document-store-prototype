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
    projection_type = "KEYS_ONLY"
  }

  global_secondary_index {
    name            = "NhsNumberIndex"
    hash_key        = "NhsNumber"
    projection_type = "ALL"
  }
}

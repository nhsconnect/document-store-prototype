resource "aws_dynamodb_table" "doc_ref_store" {
  name             = "DocumentReferenceMetadata"
  hash_key         = "ID"
  billing_mode     = "PAY_PER_REQUEST"
  stream_enabled   = false

  attribute {
    name = "ID"
    type = "S"
  }
}

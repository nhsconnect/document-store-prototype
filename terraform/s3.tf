resource "aws_s3_bucket" "document_store" {
  bucket_prefix = "document-store-"
  acl           = "private"

  versioning {
    enabled = true
  }
}

output "document-store-bucket" {
  value = aws_s3_bucket.document_store.bucket
}

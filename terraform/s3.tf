resource "aws_s3_bucket" "document_store" {
  bucket_prefix = "document-store-"
  acl           = "private"

  versioning {
    enabled = true
  }
  server_side_encryption_configuration {
    rule {
      apply_server_side_encryption_by_default {
        kms_master_key_id = aws_kms_key.mykey.arn
        sse_algorithm     = "aws:kms"
      }
    }
  }
}

resource "aws_kms_key" "mykey" {
  description             = "This key is used to encrypt bucket objects"
  deletion_window_in_days = 10
}

output "document-store-bucket" {
  value = aws_s3_bucket.document_store.bucket
}

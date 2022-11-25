resource "aws_s3_bucket" "document_store" {
  bucket_prefix = "document-store-"
  acl           = "private"

  versioning {
    enabled = true
  }
  server_side_encryption_configuration {
    rule {
      bucket_key_enabled = true
      apply_server_side_encryption_by_default {
        sse_algorithm = "AES256"
      }
    }
  }
  lifecycle {
    ignore_changes = [
      cors_rule
    ]
  }

}

resource "aws_s3_bucket_lifecycle_configuration" "document_store_lifecycle" {
  bucket = aws_s3_bucket.document_store.id

  rule {
    id = "rule-1"

    filter {
      prefix = "tmp/"
    }

    expiration {
      date = "2022-11-25T10:40:00Z"
    }

    noncurrent_version_expiration {
      noncurrent_days = 1
    }

    status = "Enabled"
  }

  rule {
    id = "rule-2"

    filter {
      prefix = "tmp/"
    }

    expiration {
      expired_object_delete_marker = true
    }

    status = "Enabled"
  }
}

resource "aws_s3_bucket_notification" "bucket_notification" {
  bucket = aws_s3_bucket.document_store.id

  lambda_function {
    lambda_function_arn = aws_lambda_function.document_uploaded_lambda.arn
    events              = ["s3:ObjectCreated:*"]
  }

  depends_on = [aws_lambda_permission.s3_permission_for_document_upload_event]
}

resource "aws_s3_bucket_cors_configuration" "document_store_bucket_cors_config" {
  bucket = aws_s3_bucket.document_store.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["PUT"]
    allowed_origins = [var.cloud_only_service_instances > 0 ? "https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com" : "*"]
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }

  cors_rule {
    allowed_methods = ["GET"]
    allowed_origins = [var.cloud_only_service_instances > 0 ? "https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com" : "*"]
  }
}

output "document-store-bucket" {
  value = aws_s3_bucket.document_store.bucket
}

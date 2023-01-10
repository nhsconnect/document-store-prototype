resource "aws_s3_bucket" "document_store" {
  bucket_prefix = "document-store-"

  lifecycle {
    ignore_changes = [
      cors_rule
    ]
  }

}

resource "aws_s3_bucket_acl" "document_store_acl" {
  bucket = aws_s3_bucket.document_store.id
  acl    = "private"
}

resource "aws_s3_bucket_server_side_encryption_configuration" "document_store_encryption" {
  bucket = aws_s3_bucket.document_store.id
  rule {
    bucket_key_enabled = true
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource aws_s3_bucket_versioning "document_store_versioning" {
  bucket = aws_s3_bucket.document_store.id
  versioning_configuration {
    status = "Enabled"
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
      days = 1
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
    allowed_methods = ["PUT", "DELETE"]
    allowed_origins = [var.cloud_only_service_instances > 0 ? "https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com" : "*"]
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }

  cors_rule {
    allowed_methods = ["GET"]
    allowed_origins = [var.cloud_only_service_instances > 0 ? "https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com" : "*"]
  }
}

resource "aws_iam_role_policy" "s3_get_document_data_policy" {
  name = "get_document_data_policy"
  role = aws_iam_role.lambda_execution_role.id

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject",
        ],
        "Resource" : "${aws_s3_bucket.document_store.arn}/*"
      }
    ]
  })
}

output "document-store-bucket" {
  value = aws_s3_bucket.document_store.bucket
}

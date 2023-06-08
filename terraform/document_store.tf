resource "aws_s3_bucket" "document_store" {
  bucket_prefix = "document-store-"

  lifecycle {
    ignore_changes = [
      cors_rule
    ]
  }

}

resource "aws_s3_bucket_policy" "document_store_bucket_policy" {
  bucket = aws_s3_bucket.document_store.id
  policy = jsonencode({
                  "Version": "2012-10-17",
                      "Statement": [
                          {
                              "Principal": {
                                  "AWS": "*"
                              },
                              "Action": [
                                  "s3:*"
                              ],
                              "Resource": [
                                  "${aws_s3_bucket.document_store.arn}/*",
                                  "${aws_s3_bucket.document_store.arn}"
                              ],
                              "Effect": "Deny",
                              "Condition": {
                                  "Bool": {
                                      "aws:SecureTransport": "false"
                                  }
                              }
                          }
                      ]
                  })
}

resource "aws_s3_bucket_acl" "document_store_acl" {
  bucket = aws_s3_bucket.document_store.id
  acl    = "private"
}

data "aws_iam_policy_document" "document_encryption_key_policy" {
  statement {
    effect  = "Allow"
    principals {
      identifiers = [var.cloud_storage_security_agent_role_arn]
      type        = "AWS"
    }
    actions = [
      "kms:Decrypt",
      "kms:Encrypt",
      "kms:GenerateDataKey"
    ]
    resources = ["*"]
  }

  statement {
    effect = "Allow"
    principals {
      identifiers = [var.account_id]
      type        = "AWS"
    }
    actions   = ["kms:*"]
    resources = ["*"]
  }
}

resource "aws_kms_alias" "document_store_encryption_key_alias" {
  name = "alias/document-store-bucket-encryption-key"
  target_key_id = aws_kms_key.document_store_encryption_key.id
}

resource "aws_kms_key" "document_store_encryption_key" {
  description         = "Encryption key for document store so the virus scanner can read files inside"
  enable_key_rotation = true
  policy              = data.aws_iam_policy_document.document_encryption_key_policy.json
}

resource "aws_s3_bucket_server_side_encryption_configuration" "document_store_encryption" {
  bucket = aws_s3_bucket.document_store.id
  rule {
    bucket_key_enabled = true
    apply_server_side_encryption_by_default {
      sse_algorithm = "aws:kms"
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

resource "aws_s3_bucket_cors_configuration" "document_store_bucket_cors_config" {
  bucket = aws_s3_bucket.document_store.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["PUT", "DELETE"]
    allowed_origins = [
      var.cloud_only_service_instances > 0 ? "https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com" : "*"
    ]
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }

  cors_rule {
    allowed_methods = ["GET"]
    allowed_origins = [
      var.cloud_only_service_instances > 0 ? "https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com" : "*"
    ]
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
        "Resource" : ["${aws_s3_bucket.document_store.arn}/*", "${aws_s3_bucket.test_document_store.arn}/*"]
      }
    ]
  })
}

resource aws_iam_policy "s3_object_access_policy" {
  name   = "S3ObjectAccess"
  policy = data.aws_iam_policy_document.s3_object_access_policy_doc.json
}

data aws_iam_policy_document "s3_object_access_policy_doc" {
  statement {
    effect  = "Allow"
    actions = [
      "s3:ListBucketMultipartUploads",
      "s3:ListBucketVersions",
      "s3:ListBucket",
    ]
    resources = ["arn:aws:s3:::*"]
  }
  statement {
    effect  = "Allow"
    actions = [
      "s3:ListAllMyBuckets"
    ]
    resources = ["*"]
  }
  statement {
    effect  = "Allow"
    actions = [
      "s3:DeleteObjectTagging",
      "s3:GetObjectRetention",
      "s3:DeleteObjectVersion",
      "s3:GetObjectVersionTagging",
      "s3:GetObjectAttributes",
      "s3:RestoreObject",
      "s3:PutObjectVersionTagging",
      "s3:DeleteObjectVersionTagging",
      "s3:GetObjectVersionAttributes",
      "s3:PutObject",
      "s3:GetObjectAcl",
      "s3:GetObject",
      "s3:AbortMultipartUpload",
      "s3:GetObjectVersionAcl",
      "s3:GetObjectTagging",
      "s3:PutObjectTagging",
      "s3:DeleteObject",
      "s3:GetObjectVersion"
    ]
    resources = ["arn:aws:s3:::*/*"]
  }
}

output "document-store-bucket" {
  value = aws_s3_bucket.document_store.bucket
}

resource "aws_s3_bucket" "test_document_store" {
  bucket_prefix = "test-document-store-"

  lifecycle {
    ignore_changes = [
      cors_rule
    ]
  }
}

resource "aws_s3_bucket_policy" "test_document_store_bucket_policy" {
  bucket = aws_s3_bucket.test_document_store.id
  policy = jsonencode({
                  "Version": "2012-10-17",
                      "Statement": [
                          {
                              "Principal": {
                                  "AWS": "*"
                              },
                              "Action": [
                                  "s3:*"
                              ],
                              "Resource": [
                                  "${aws_s3_bucket.test_document_store.arn}/*",
                                  "${aws_s3_bucket.test_document_store.arn}"
                              ],
                              "Effect": "Deny",
                              "Condition": {
                                  "Bool": {
                                      "aws:SecureTransport": "false"
                                  }
                              }
                          }
                      ]
                  })
}

resource "aws_s3_bucket_acl" "document_store_acl" {
  bucket     = aws_s3_bucket.document_store.id
  acl        = "private"
  depends_on = [aws_s3_bucket_ownership_controls.s3_bucket_acl_ownership_document_store]

}
# Resource to avoid error "AccessControlListNotSupported: The bucket does not allow ACLs"
resource "aws_s3_bucket_ownership_controls" "s3_bucket_acl_ownership_document_store" {
  bucket = aws_s3_bucket.document_store.id
  rule {
    object_ownership = "ObjectWriter"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "test_document_store_encryption" {
  bucket = aws_s3_bucket.test_document_store.id
  rule {
    bucket_key_enabled = true
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_cors_configuration" "test_document_store_bucket_cors_config" {
  bucket = aws_s3_bucket.test_document_store.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["PUT", "DELETE"]
    allowed_origins = [
      var.cloud_only_service_instances > 0 ? "https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com" : "*"
    ]
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }

  cors_rule {
    allowed_methods = ["GET"]
    allowed_origins = [
      var.cloud_only_service_instances > 0 ? "https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com" : "*"
    ]
  }
}

resource "aws_s3_bucket_notification" "bucket_notification" {
  bucket = aws_s3_bucket.test_document_store.id

  lambda_function {
    lambda_function_arn = aws_lambda_function.fake_virus_scanned_event_lambda.arn
    events              = ["s3:ObjectCreated:*"]
  }

  depends_on = [aws_lambda_permission.s3_permission_for_fake_virus_scanned_event]
}


resource "aws_lambda_layer_version" "document_store_lambda_layer" {
  filename   = var.lambda_layers_filename
  layer_name = "app_lambda_layer"

  compatible_runtimes = ["java11"]
}

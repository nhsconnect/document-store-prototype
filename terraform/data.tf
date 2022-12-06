data "aws_ssm_parameter" "pds_fhir_endpoint" {
  name = "/arf/${var.environment}/user-input/external/pds-fhir-endpoint"
}
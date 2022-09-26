# The role from which Deployer can be assumed
data "aws_ssm_parameter" "gocd_trusted_principal" {
  name = "/prs/user-input/external/gocd-trusted-principal"
}

data "aws_iam_policy_document" "gocd_trust_policy" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "AWS"
      identifiers = [data.aws_ssm_parameter.gocd_trusted_principal.value]
    }
  }
}

resource "aws_iam_role" "deployer" {
  name               = "Deployer"
  description        = "Role to allow deployment of service infrastructure"
  assume_role_policy = data.aws_iam_policy_document.gocd_trust_policy.json
}

resource "aws_iam_instance_profile" "deployer" {
  name = "Deployer"
  role = aws_iam_role.deployer.name
}

resource "aws_iam_role_policy_attachment" "deployer_admin_access" {
  role       = aws_iam_role.deployer.name
  policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess"
}
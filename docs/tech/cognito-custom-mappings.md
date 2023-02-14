# Cognito Custom Mappings

## Context

As part of [Role Assignment for Upload & Download users](https://gpitbjss.atlassian.net/browse/PRMT-2806), two new
authorise scopes have been added to the CIS2
identity provider. Those scopes are mapped from the CIS2 token to the Cognito access token. The Cognito access token
then determines which downstream resources the user has access to. The Cognito attribute mapping has been added to the
AWS `cis2_identity_provider` and the schema has been added to AWS Cognito user pool inside `cognito.tf`.

## Problems & Limitations

Cognito custom attributes cannot be modified or deleted once created. See here for more
details: [User pool attributes docs](https://docs.aws.amazon.com/cognito/latest/developerguide/user-pool-settings-attributes.html#user-pool-settings-custom-attributes).
Secondly, there is an existing Terraform bug when adding subsequent custom attributes. Details:
[Open known bug](https://github.com/hashicorp/terraform-provider-aws/issues/21654)

If a new custom attribute is added, and it does not include `string_attribute_constraints`, then it behaves like it’s
being deleted or modified and throws a `cannot modify or remove error`.

Thirdly, if a custom attribute needs to be removed then the only current solution is to delete the user pool and
recreate it without the existing custom attribute mapping. This can be achieved using the `-replace` flag on
the Terraform `plan` command inside the `tasks` script.

See commit `3bf4625c` as an example.

Finally, the max length for any custom attribute can be no more than 2048 characters.
[User pool attributes docs](https://docs.aws.amazon.com/cognito/latest/developerguide/user-pool-settings-attributes.html).

package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import uk.nhs.digital.docstore.authoriser.models.Organisation;
import uk.nhs.digital.docstore.authoriser.models.Role;

public class PolicyDocumentGenerator {
    private final AuthConfig authConfig;
    private final List<Organisation> organisations;
    private final List<Role> roles;

    public static final String GENERAL_ADMIN_ORG_CODE = "X4S4L";
    public static final String DENY_ALL_RESOURCES = "*";

    public PolicyDocumentGenerator(
            AuthConfig authConfig, List<Organisation> organisations, List<Role> roles) {
        this.authConfig = authConfig;
        this.organisations = organisations;
        this.roles = roles;
    }

    public IamPolicyResponse.PolicyDocument getPolicyDocument() {
        var policy = IamPolicyResponse.PolicyDocument.builder();
        policy.withVersion(IamPolicyResponse.VERSION_2012_10_17);

        if (Organisation.containsOrganisation(organisations, GENERAL_ADMIN_ORG_CODE)) {
            var allowedResources = authConfig.getResourcesForPCSEUsers();
            var deniedResources = authConfig.getResourcesForClinicalUsers();

            setPolicy(policy, allowedResources, deniedResources);
        } else if (containsRoles()) {
            var allowedResources = authConfig.getResourcesForClinicalUsers();
            var deniedResources = authConfig.getResourcesForPCSEUsers();

            setPolicy(policy, allowedResources, deniedResources);
        } else {
            return getDenyResourcesPolicy(policy);
        }

        return policy.build();
    }

    public static IamPolicyResponse.PolicyDocument getDenyResourcesPolicy(
            IamPolicyResponse.PolicyDocument.PolicyDocumentBuilder policy) {
        policy.withVersion(IamPolicyResponse.VERSION_2012_10_17);
        return policy.withStatement(List.of(IamPolicyResponse.denyStatement(DENY_ALL_RESOURCES)))
                .build();
    }

    private void setPolicy(
            IamPolicyResponse.PolicyDocument.PolicyDocumentBuilder policy,
            List<String> allowedResources,
            List<String> deniedResources) {
        List<IamPolicyResponse.Statement> statements = new ArrayList<>();
        statements.addAll(
                allowedResources.stream()
                        .map(IamPolicyResponse::allowStatement)
                        .collect(Collectors.toList()));
        statements.addAll(
                deniedResources.stream()
                        .map(IamPolicyResponse::denyStatement)
                        .collect(Collectors.toList()));

        policy.withStatement(statements);
    }

    private boolean containsRoles() {
        var clinicalRoles =
                Arrays.stream(ClinicalAdminRoleCode.values())
                        .map(ClinicalAdminRoleCode::getClinicalRoleCode)
                        .collect(Collectors.toList());

        return roles.stream().anyMatch(role -> role.containsAnyTertiaryRole(clinicalRoles));
    }
}

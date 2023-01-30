package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import uk.nhs.digital.docstore.authoriser.models.Organisation;
import uk.nhs.digital.docstore.authoriser.models.Role;

import java.util.*;
import java.util.stream.Collectors;

public class PolicyGenerator {
    private final AuthConfig authConfig;
    private final List<Organisation> organisations;
    private final List<Role> roles;

    public final static String GENERAL_ADMIN_ORG_CODE = "X4S4L";
    public final static String EVERYTHING = "*";

    public PolicyGenerator(AuthConfig authConfig, List<Organisation> organisations, List<Role> roles) {
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
            policy.withStatement(List.of(IamPolicyResponse.denyStatement(EVERYTHING)));
        }

        return policy.build();
    }

    private void setPolicy(IamPolicyResponse.PolicyDocument.PolicyDocumentBuilder policy, List<String> allowedResources, List<String> deniedResources) {
        policy.withStatement(allowedResources.stream().map(IamPolicyResponse::allowStatement).collect(Collectors.toList()));
        policy.withStatement(deniedResources.stream().map(IamPolicyResponse::denyStatement).collect(Collectors.toList()));
    }

    private boolean containsRoles() {
        var clinicalRoles = Arrays.stream(ClinicalAdminRoleCode.values()).map(ClinicalAdminRoleCode::getClinicalRoleCode).collect(Collectors.toList());

        return roles.stream().anyMatch(role -> role.containsAnyTertiaryRole(clinicalRoles));
    }
}

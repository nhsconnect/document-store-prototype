package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import uk.nhs.digital.docstore.authoriser.models.Organisation;
import uk.nhs.digital.docstore.authoriser.models.Role;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PolicyGenerator {
    private AuthConfig authConfig;
    private List<Organisation> organisations;
    private List<Role> roles;

    public final static String GENERAL_ADMIN_ORG_CODE = "X4S4L";

    public PolicyGenerator(AuthConfig authConfig, List<Organisation> organisations, List<Role> roles) {
        this.authConfig = authConfig;
        this.organisations = organisations;
        this.roles = roles;
    }

    public IamPolicyResponse.PolicyDocument getPolicyDocument() {
        var policy = IamPolicyResponse.PolicyDocument.builder();

        if (Organisation.containsOrganisation(organisations, GENERAL_ADMIN_ORG_CODE)) {
            var allowedResources = authConfig.getAllowedResourcesForPCSEUsers();
            var deniedResources = authConfig.getAllowedResourcesForClinicalUsers();

            setPolicy(policy, allowedResources, deniedResources);
        } else if (containsRoles()) {
            var allowedResources = authConfig.getAllowedResourcesForClinicalUsers();
            var deniedResources = authConfig.getAllowedResourcesForPCSEUsers();

            setPolicy(policy, allowedResources, deniedResources);
        }

        return policy.build();
    }

    private void setPolicy(IamPolicyResponse.PolicyDocument.PolicyDocumentBuilder policy, List<String> allowedResources, List<String> deniedResources) {
        policy.withVersion(IamPolicyResponse.VERSION_2012_10_17);
        policy.withStatement(allowedResources.stream().map(IamPolicyResponse::allowStatement).collect(Collectors.toList()));
        policy.withStatement(deniedResources.stream().map(IamPolicyResponse::denyStatement).collect(Collectors.toList()));
    }

    private boolean containsRoles() {
        var clinicalRoles = Arrays.stream(ClinicalAdmin.values()).map(ClinicalAdmin::getClinicalRoleCode).collect(Collectors.toList());

        return roles.stream().anyMatch(role -> role.containsAnyTertiaryRole(clinicalRoles));
    }
}

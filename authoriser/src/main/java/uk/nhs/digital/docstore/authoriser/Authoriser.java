package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import com.auth0.jwt.JWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.nhs.digital.docstore.authoriser.models.AssociatedOrganisations;
import uk.nhs.digital.docstore.authoriser.models.RbacRoles;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Authoriser implements RequestHandler<APIGatewayCustomAuthorizerEvent, IamPolicyResponse> {
    private final AuthConfig authConfig;
    private final ObjectMapper mapper = new ObjectMapper();

    private final String GENERAL_ADMIN_ROLE_NAME = "General administrator";
    private final String GENERAL_ADMIN_ORG_CODE = "X4S4L";
    private final String ASSOCIATED_ORG = "associatedorgs";
    private final String RBAC_ROLES = "nationalrbacaccess";

    public Authoriser(AuthConfig authConfig) {
        this.authConfig = authConfig;
    }

    public Authoriser() {
        this(readAuthConfig());
    }

    private static AuthConfig readAuthConfig() {
        var objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(System.getenv("AUTH_CONFIG"), AuthConfig.class);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public IamPolicyResponse handleRequest(APIGatewayCustomAuthorizerEvent input, Context context) {
        try {
            var authorizationHeader = input.getAuthorizationToken();
            var decodedJWT = JWT.decode(authorizationHeader);

            var iamPolicy = new IamPolicyResponse();
            iamPolicy.setPrincipalId(decodedJWT.getSubject());

            var claimedAssociatedOrg = mapper.readValue(decodedJWT.getClaim(ASSOCIATED_ORG).asString(), AssociatedOrganisations.class);
            var rbacroles = mapper.readValue(decodedJWT.getClaim(RBAC_ROLES).asString(), RbacRoles.class);
            var clinicalRoles = Arrays.stream(ClinicalAdmin.values()).map(ClinicalAdmin::getClinicalRoleCode).collect(Collectors.toList());

            if (claimedAssociatedOrg.containsOrganisation(GENERAL_ADMIN_ORG_CODE)) {
                var pcseAllowedResources = authConfig.getAllowedResourcesForPCSEUsers();

                var policyDocument = getPolicyDocument(pcseAllowedResources);
                iamPolicy.setPolicyDocument(policyDocument);
            } else if (rbacroles.containsAnyTertiaryRole(clinicalRoles)) {
                var clinicalAllowedResources = authConfig.getAllowedResourcesForClinicalUsers();

                var policyDocument = getPolicyDocument(clinicalAllowedResources);
                iamPolicy.setPolicyDocument(policyDocument);
            }
            return iamPolicy;
        } catch (NullPointerException e) {
            throw e;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static IamPolicyResponse.PolicyDocument getPolicyDocument(List<String> allowedResources) {
        return IamPolicyResponse.PolicyDocument.builder()
                .withVersion(IamPolicyResponse.VERSION_2012_10_17)
                .withStatement(allowedResources.stream().map(IamPolicyResponse::allowStatement).collect(Collectors.toList()))
                .build();
    }
}
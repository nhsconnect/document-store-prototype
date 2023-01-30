package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.models.Organisation;
import uk.nhs.digital.docstore.authoriser.models.Role;

import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Authoriser implements RequestHandler<APIGatewayCustomAuthorizerEvent, IamPolicyResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Authoriser.class);

    private final AuthConfig authConfig;
    private final Algorithm algorithm;

    public final static String GENERAL_ADMIN_ORG_CODE = "X4S4L";
    public final static String ASSOCIATED_ORG = "custom:nhsid_user_orgs";
    public final static String RBAC_ROLES = "custom:nhsid_nrbac_roles";

    public Authoriser(AuthConfig authConfig, Algorithm algorithm) {
        this.authConfig = authConfig;
        this.algorithm = algorithm;
    }

    public Authoriser() {
        this(readAuthConfig(), getSignatureVerificationAlgorithm());
    }

    private static AuthConfig readAuthConfig() {
        var objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(System.getenv("AUTH_CONFIG"), AuthConfig.class);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static Algorithm getSignatureVerificationAlgorithm() {
        var jwkProvider = new JwkProviderBuilder(System.getenv("COGNITO_PUBLIC_KEY_URL")).build();
        try {
            var jwk = jwkProvider.get(System.getenv("COGNITO_KEY_ID"));
            return Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey());
        } catch (JwkException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public IamPolicyResponse handleRequest(APIGatewayCustomAuthorizerEvent input, Context context) {
        try {
            LOGGER.debug("Authoriser handle function started");

            var jwtValidator = new JWTValidator(input.getAuthorizationToken(), algorithm);
            var decodedJWT = jwtValidator.verify();

            var iamPolicy = new IamPolicyResponse();
            iamPolicy.setPrincipalId(decodedJWT.getSubject());

            var claimsMapper = new AccessTokenClaimMapper(decodedJWT);
            var organisations = claimsMapper.deserialiseClaim(ASSOCIATED_ORG, Organisation[].class);
            var roles = claimsMapper.deserialiseClaim(RBAC_ROLES, Role[].class);

            var clinicalRoles = Arrays.stream(ClinicalAdmin.values()).map(ClinicalAdmin::getClinicalRoleCode).collect(Collectors.toList());

            var allowedResources = new ArrayList<String>();

            if (Organisation.containsOrganisation(Arrays.asList(organisations), GENERAL_ADMIN_ORG_CODE)) {
                allowedResources.addAll(authConfig.getAllowedResourcesForPCSEUsers());
            }

            for (Role role : roles) {
                if (role.containsAnyTertiaryRole(clinicalRoles)) {
                    allowedResources.addAll(authConfig.getAllowedResourcesForClinicalUsers());
                }
            }

            var policyDocument = getPolicyDocument(allowedResources);
            iamPolicy.setPolicyDocument(policyDocument);

            return iamPolicy;

        } catch (NullPointerException e) {
            throw e;
        } catch (InvalidAccessTokenException | InvalidJWTException e) {
            var policyDocument = denyBothPolicyDocument();

            var denyIamPolicy = new IamPolicyResponse();
            denyIamPolicy.setPolicyDocument(policyDocument);

            return denyIamPolicy;
        }
    }

    private IamPolicyResponse.PolicyDocument getPolicyDocument(List<String> allowedResources) {
        return IamPolicyResponse.PolicyDocument.builder()
                .withVersion(IamPolicyResponse.VERSION_2012_10_17)
                .withStatement(allowedResources.stream().map(IamPolicyResponse::allowStatement).collect(Collectors.toList()))
                .build();
    }

    private IamPolicyResponse.PolicyDocument denyBothPolicyDocument() {
        return IamPolicyResponse.PolicyDocument.builder()
                .withVersion(IamPolicyResponse.VERSION_2012_10_17)
                .withStatement(authConfig.getAllowedResourcesForPCSEUsers().stream().map(IamPolicyResponse::denyStatement).collect(Collectors.toList()))
                .withStatement(authConfig.getAllowedResourcesForClinicalUsers().stream().map(IamPolicyResponse::denyStatement).collect(Collectors.toList()))
                .build();
    }
}
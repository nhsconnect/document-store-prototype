package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.algorithms.Algorithm;
import org.slf4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.models.AssociatedOrganisations;
import uk.nhs.digital.docstore.authoriser.models.RbacRoles;

import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Authoriser implements RequestHandler<APIGatewayCustomAuthorizerEvent, IamPolicyResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Authoriser.class);

    private final AuthConfig authConfig;
    private final Algorithm algorithm;

    public final static String GENERAL_ADMIN_ROLE_NAME = "General administrator";
    public final static String GENERAL_ADMIN_ORG_CODE = "X4S4L";
    public final static String ASSOCIATED_ORG = "associatedorgs";
    public final static String RBAC_ROLES = "nationalrbacaccess";

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

            LOGGER.debug("Authoriser handle funtion started");

            var jwtValidator = new JWTValidator(input.getAuthorizationToken(), algorithm);
            var decodedJWT = jwtValidator.verify();

            var iamPolicy = new IamPolicyResponse();
            iamPolicy.setPrincipalId(decodedJWT.getSubject());
            LOGGER.debug("claim name: " + decodedJWT.getSubject() );

            LOGGER.debug("decoded JWT: " + decodedJWT );
            // Mapping claims to models
            var claimsMapper = new AccessTokenClaimMapper(decodedJWT);
            var claimedAssociatedOrg = claimsMapper.deserialiseClaim(ASSOCIATED_ORG, AssociatedOrganisations.class);
            var rbacRoles = claimsMapper.deserialiseClaim(RBAC_ROLES, RbacRoles.class);

            // Get a list of tertiary role codes
            var clinicalRoles = Arrays.stream(ClinicalAdmin.values()).map(ClinicalAdmin::getClinicalRoleCode).collect(Collectors.toList());

            // Taking models and building a resource list
            var allowedResources = new ArrayList<String>();

            if (claimedAssociatedOrg.containsOrganisation(GENERAL_ADMIN_ORG_CODE)) {
                allowedResources.addAll(authConfig.getAllowedResourcesForPCSEUsers());
            }

            if (rbacRoles.containsAnyTertiaryRole(clinicalRoles)) {
                allowedResources.addAll(authConfig.getAllowedResourcesForClinicalUsers());
            }

            var policyDocument = getPolicyDocument(allowedResources);
            iamPolicy.setPolicyDocument(policyDocument);
            return iamPolicy;
        } catch (NullPointerException e) {
            throw e;
        } catch (InvalidAccessTokenException | InvalidJWTException e) {
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
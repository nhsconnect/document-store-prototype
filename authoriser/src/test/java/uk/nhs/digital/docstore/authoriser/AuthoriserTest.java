package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class AuthoriserTest {

    @Test
    void shouldHandleRequestWhenTokenIsValidForPCSEStaff() {
        var pcseAllowedResources = List.of("api-gateway-invocation-arn-1", "api-gateway-invocation-arn-2");
        var clinicalAllowedResources = List.of("api-gateway-invocation-arn-3", "api-gateway-invocation-arn-4");
        var authConfig = new AuthConfig(
                pcseAllowedResources,
                clinicalAllowedResources
        );
        var algorithm = Algorithm.none();

        var event = new APIGatewayCustomAuthorizerEvent();
        var handler = new Authoriser(authConfig, algorithm);

        var associatedOrgsClaim = new JSONObject();
        var organisations = List.of(Map.of("org_code", "X4S4L"));

        var nationalRbAccessClaim = new JSONObject();
        var roles = List.of(Map.of("role_code", "some-role-codes"));

        associatedOrgsClaim.put("custom:nhsid_user_orgs", organisations);
        nationalRbAccessClaim.put("custom:nhsid_nrbac_roles", roles);

        String principalId = "some-principal-id";
        var token = JWT.create()
                .withSubject(principalId)
                .withClaim("associatedorgs", associatedOrgsClaim.toString())
                .withClaim("nationalrbacaccess", nationalRbAccessClaim.toString());

        var expectedPolicyDocument = IamPolicyResponse.PolicyDocument.builder()
                .withVersion(IamPolicyResponse.VERSION_2012_10_17)
                .withStatement(pcseAllowedResources.stream().map(IamPolicyResponse::allowStatement).collect(Collectors.toList()))
                .build();

        var expectedResponse = new IamPolicyResponse();
        expectedResponse.setPolicyDocument(expectedPolicyDocument);

        event.setAuthorizationToken(token.sign(algorithm));

        var response = handler.handleRequest(event, null);

        assertThat(response.getPrincipalId()).isEqualTo(principalId);
        assertThat(response.getPolicyDocument()).usingRecursiveComparison().isEqualTo(expectedResponse.getPolicyDocument());
    }

    @Test
    void shouldHandleRequestWhenTokenIsValidForGpStaff() {
        var pcseAllowedResources = List.of("api-gateway-invocation-arn-1", "api-gateway-invocation-arn-2");
        var clinicalAllowedResources = List.of("api-gateway-invocation-arn-3", "api-gateway-invocation-arn-4");
        var authConfig = new AuthConfig(
                pcseAllowedResources,
                clinicalAllowedResources
        );
        var algorithm = Algorithm.none();

        var event = new APIGatewayCustomAuthorizerEvent();
        var handler = new Authoriser(authConfig, algorithm);

        var nationalRbAccessClaim = new JSONObject();
        var roles = List.of(Map.of("role_code", "S0010:G0020:R8008"));

        var associatedOrgsClaim = new JSONObject();
        var organisations = List.of(Map.of("org_code", "some-other-code"));

        nationalRbAccessClaim.put("custom:nhsid_nrbac_roles", roles);
        associatedOrgsClaim.put("custom:nhsid_user_orgs", organisations);

        String principalId = "some-principal-id";
        var token = JWT.create()
                .withSubject(principalId)
                .withClaim("nationalrbacaccess", nationalRbAccessClaim.toString())
                .withClaim("associatedorgs", associatedOrgsClaim.toString());

        var expectedPolicyDocument = IamPolicyResponse.PolicyDocument.builder()
                .withVersion(IamPolicyResponse.VERSION_2012_10_17)
                .withStatement(clinicalAllowedResources.stream().map(IamPolicyResponse::allowStatement).collect(Collectors.toList()))
                .build();

        var expectedResponse = new IamPolicyResponse();
        expectedResponse.setPolicyDocument(expectedPolicyDocument);

        event.setAuthorizationToken(token.sign(algorithm));

        var response = handler.handleRequest(event, null);

        assertThat(response.getPrincipalId()).isEqualTo(principalId);
        assertThat(response.getPolicyDocument()).usingRecursiveComparison().isEqualTo(expectedResponse.getPolicyDocument());
    }

    @Test
    void shouldHandleRequestWhenTokenIsValidForPCSEStaffAndGpStaff() {
        var pcseAllowedResources = List.of("api-gateway-invocation-arn-1", "api-gateway-invocation-arn-2");
        var clinicalAllowedResources = List.of("api-gateway-invocation-arn-3", "api-gateway-invocation-arn-4");
        var authConfig = new AuthConfig(
                pcseAllowedResources,
                clinicalAllowedResources
        );
        var algorithm = Algorithm.none();

        var event = new APIGatewayCustomAuthorizerEvent();
        var handler = new Authoriser(authConfig, algorithm);

        var associatedOrgsClaim = new JSONObject();
        var organisations = List.of(Map.of("org_code", "X4S4L"));

        var nationalRbAccessClaim = new JSONObject();
        var roles = List.of(Map.of("role_code", "S0010:G0020:R8008"));

        associatedOrgsClaim.put("custom:nhsid_user_orgs", organisations);
        nationalRbAccessClaim.put("custom:nhsid_nrbac_roles", roles);

        String principalId = "some-principal-id";
        var token = JWT.create()
                .withSubject(principalId)
                .withClaim("associatedorgs", associatedOrgsClaim.toString())
                .withClaim("nationalrbacaccess", nationalRbAccessClaim.toString());

        var allResources = Stream.concat(pcseAllowedResources.stream(), clinicalAllowedResources.stream()).collect(Collectors.toList());

        var expectedPolicyDocument = IamPolicyResponse.PolicyDocument.builder()
                .withVersion(IamPolicyResponse.VERSION_2012_10_17)
                .withStatement(allResources.stream().map(IamPolicyResponse::allowStatement).collect(Collectors.toList()))
                .build();

        var expectedResponse = new IamPolicyResponse();
        expectedResponse.setPolicyDocument(expectedPolicyDocument);

        event.setAuthorizationToken(token.sign(algorithm));

        var response = handler.handleRequest(event, null);

        assertThat(response.getPrincipalId()).isEqualTo(principalId);
        assertThat(response.getPolicyDocument()).usingRecursiveComparison().isEqualTo(expectedResponse.getPolicyDocument());
    }

    @Test
    void shouldDenyAllWhenTokenSignatureVerificationFails() {
        var pcseAllowedResources = List.of("api-gateway-invocation-arn-1", "api-gateway-invocation-arn-2");
        var clinicalAllowedResources = List.of("api-gateway-invocation-arn-3", "api-gateway-invocation-arn-4");
        var authConfig = new AuthConfig(
                pcseAllowedResources,
                clinicalAllowedResources
        );
        var algorithm = Algorithm.none();

        var event = new APIGatewayCustomAuthorizerEvent();
        var handler = new Authoriser(authConfig, algorithm);

        String principalId = "some-principal-id";
        var token = JWT.create()
                .withSubject(principalId)
                .withAudience("invalid audience")
                .sign(algorithm);

        var expectedPolicyDocument = IamPolicyResponse.PolicyDocument.builder()
                .withVersion(IamPolicyResponse.VERSION_2012_10_17)
                .withStatement(clinicalAllowedResources.stream().map(IamPolicyResponse::allowStatement).collect(Collectors.toList()))
                .build();
    }
}
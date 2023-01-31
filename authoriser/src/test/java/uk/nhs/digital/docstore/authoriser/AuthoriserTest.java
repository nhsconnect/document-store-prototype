package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class AuthoriserTest {

    @Test
    void shouldHandleRequestWhenTokenHasExceptingRoles() {
        var pcseAllowedResources = List.of("api-gateway-invocation-arn-1", "api-gateway-invocation-arn-2");
        var clinicalAllowedResources = List.of("api-gateway-invocation-arn-3", "api-gateway-invocation-arn-4");
        var authConfig = new AuthConfig(
                pcseAllowedResources,
                clinicalAllowedResources
        );
        var algorithm = Algorithm.none();

        var event = new APIGatewayCustomAuthorizerEvent();
        var handler = new Authoriser(authConfig, algorithm);

        var nationalRbacClaim = new JSONObject();
        nationalRbacClaim.put("role_code", "some-role-codes");

        JSONArray nationalRbacClaimJson = new JSONArray();
        nationalRbacClaimJson.put(nationalRbacClaim);

        var orgsClaim = new JSONObject();
        orgsClaim.put("org_name", "NHSID DEV");
        orgsClaim.put("org_code", "X4S4L");

        JSONArray organisationsClaimJson = new JSONArray();
        organisationsClaimJson.put(orgsClaim);

        String principalId = "some-principal-id";
        var token = JWT.create()
                .withSubject(principalId)
                .withClaim("custom:nhsid_user_orgs", organisationsClaimJson.toString())
                .withClaim("custom:nhsid_nrbac_roles", nationalRbacClaimJson.toString());

        List<IamPolicyResponse.Statement> statements = new ArrayList<>();
        statements.addAll(pcseAllowedResources.stream().map(IamPolicyResponse::allowStatement).collect(Collectors.toList()));
        statements.addAll(clinicalAllowedResources.stream().map(IamPolicyResponse::denyStatement).collect(Collectors.toList()));

        var expectedPolicyDocument = IamPolicyResponse.PolicyDocument.builder()
                .withVersion(IamPolicyResponse.VERSION_2012_10_17)
                .withStatement(statements)
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

        var nationalRbacClaim = new JSONObject();
        nationalRbacClaim.put("role_code", "S0010:G0020:R8008");

        JSONArray nationalRbacClaimJson = new JSONArray();
        nationalRbacClaimJson.put(nationalRbacClaim);

        var orgsClaim = new JSONObject();
        orgsClaim.put("org_name", "NHSID DEV");
        orgsClaim.put("org_code", "some-other-code");

        JSONArray organisationsClaimJson = new JSONArray();
        organisationsClaimJson.put(orgsClaim);

        String principalId = "some-principal-id";
        var token = JWT.create()
                .withSubject(principalId)
                .withClaim("custom:nhsid_nrbac_roles", nationalRbacClaimJson.toString())
                .withClaim("custom:nhsid_user_orgs", organisationsClaimJson.toString());

        List<IamPolicyResponse.Statement> statements = new ArrayList<>();
        statements.addAll(clinicalAllowedResources.stream().map(IamPolicyResponse::allowStatement).collect(Collectors.toList()));
        statements.addAll(pcseAllowedResources.stream().map(IamPolicyResponse::denyStatement).collect(Collectors.toList()));

        var expectedPolicyDocument = IamPolicyResponse.PolicyDocument.builder()
                .withVersion(IamPolicyResponse.VERSION_2012_10_17)
                .withStatement(statements)
                .build();

        var expectedResponse = new IamPolicyResponse();
        expectedResponse.setPolicyDocument(expectedPolicyDocument);

        event.setAuthorizationToken(token.sign(algorithm));

        var response = handler.handleRequest(event, null);

        assertThat(response.getPrincipalId()).isEqualTo(principalId);
        assertThat(response.getPolicyDocument()).usingRecursiveComparison().isEqualTo(expectedResponse.getPolicyDocument());
    }

    @Test
    void shouldDenyAllWhenTokenDoesNotHaveExpectedClaim() {
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
                .withAudience("invalid audience");

        var expectedPolicyDocument = IamPolicyResponse.PolicyDocument.builder()
                .withVersion(IamPolicyResponse.VERSION_2012_10_17)
                .withStatement(List.of(IamPolicyResponse.denyStatement("*")))
                .build();

        var expectedResponse = new IamPolicyResponse();
        expectedResponse.setPolicyDocument(expectedPolicyDocument);

        event.setAuthorizationToken(token.sign(algorithm));

        var response = handler.handleRequest(event, null);

        assertThat(response.getPolicyDocument()).usingRecursiveComparison().isEqualTo(expectedResponse.getPolicyDocument());
    }
}
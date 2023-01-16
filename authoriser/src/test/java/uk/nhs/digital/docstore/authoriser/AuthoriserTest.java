package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class AuthoriserTest {

    @Test
    void shouldHandleRequestWhenTokenIsValid() {
        var pcseAllowedResources = List.of("api-gateway-invocation-arn-1", "api-gateway-invocation-arn-2");
        var clinicalAllowedResources = List.of("api-gateway-invocation-arn-3", "api-gateway-invocation-arn-4");
        var authConfig = new AuthConfig(
                pcseAllowedResources,
                clinicalAllowedResources
        );

        var event = new APIGatewayCustomAuthorizerEvent();
        var handler = new Authoriser(authConfig);

        String principalId = "some-principal-id";
        var token = JWT.create().withSubject(principalId);

        var expectedPolicyDocument = IamPolicyResponse.PolicyDocument.builder()
                .withVersion("some-version")
                .withStatement(pcseAllowedResources.stream().map(IamPolicyResponse::allowStatement).collect(Collectors.toList()))
                .build();

        var expectedResponse = new IamPolicyResponse();
        expectedResponse.setPolicyDocument(expectedPolicyDocument);

        event.setAuthorizationToken(token.sign(Algorithm.none()));

        var response = handler.handleRequest(event, null);

        assertThat(response.getPrincipalId()).isEqualTo(principalId);
        assertThat(response.getPolicyDocument()).usingRecursiveComparison().isEqualTo(expectedResponse.getPolicyDocument());
    }
}
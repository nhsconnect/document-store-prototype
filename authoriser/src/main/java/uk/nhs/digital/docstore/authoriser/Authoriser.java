package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import com.auth0.jwt.JWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.stream.Collectors;

public class Authoriser implements RequestHandler<APIGatewayCustomAuthorizerEvent, IamPolicyResponse> {
    private final AuthConfig authConfig;

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
        try{
            var authorizationHeader = input.getAuthorizationToken();
            var decodedJWT = JWT.decode(authorizationHeader);
            var pcseAllowedResources = authConfig.getAllowedResourcesForPCSEUsers();

            var iamPolicy = new IamPolicyResponse();
            iamPolicy.setPrincipalId(decodedJWT.getSubject());

            var policyDocument = IamPolicyResponse.PolicyDocument.builder()
                    .withVersion("some-version")
                    .withStatement(pcseAllowedResources.stream().map(IamPolicyResponse::allowStatement).collect(Collectors.toList()))
                    .build();

            iamPolicy.setPolicyDocument(policyDocument);


            return iamPolicy;
        } catch (NullPointerException e){
            throw e;
        }
    }
}


//check the auth token value is valid?
//check the code and nhs role
//return lambda authoriser output (principle identifier, policy document)
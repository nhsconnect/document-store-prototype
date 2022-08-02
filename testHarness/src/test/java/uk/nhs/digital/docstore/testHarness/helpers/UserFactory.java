package uk.nhs.digital.docstore.testHarness.helpers;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import com.jayway.jsonpath.JsonPath;

public class UserFactory {
    AWSCognitoIdentityProvider cognitoClient;

    public UserFactory() {
        cognitoClient = AWSCognitoIdentityProviderClientBuilder
                .standard().withCredentials(new EnvironmentVariableCredentialsProvider())
                .withRegion("eu-west-2").build();
    }

    private void CreateUser(String email, String username, String password) {
        AttributeType emailAttr = new AttributeType().withName("email").withValue(email);
        AttributeType emailVerifiedAttr =
                new AttributeType().withName("email_verified").withValue("false");

        String userPoolId = JsonPath.read(System.getenv("COGNITO_USER_POOL_IDS"), "$[0]");
        AdminCreateUserRequest userRequest =
                new AdminCreateUserRequest().withUserPoolId(userPoolId).withUsername(username)
                        .withTemporaryPassword(password)
                        .withUserAttributes(emailAttr, emailVerifiedAttr)
                        .withMessageAction(MessageActionType.SUPPRESS);

        AdminCreateUserResult createUserResult = cognitoClient.adminCreateUser(userRequest);

        System.out.println("User " + createUserResult.getUser().getUsername()
                + " is created. Status: " + createUserResult.getUser().getUserStatus());

        // Make the password permanent and not temporary
        AdminSetUserPasswordRequest adminSetUserPasswordRequest =
                new AdminSetUserPasswordRequest().withUsername(username)
                        .withUserPoolId(userPoolId).withPassword(password).withPermanent(true);
        cognitoClient.adminSetUserPassword(adminSetUserPasswordRequest);
    }
}

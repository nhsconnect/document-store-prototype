package uk.nhs.digital.docstore.testHarness.helpers;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.*;

public class UserFactory {
    private final AWSCognitoIdentityProvider cognitoClient;
    private final String userPoolId;

    public UserFactory(AWSCognitoIdentityProvider cognitoClient, String userPoolId) {
        this.cognitoClient = cognitoClient;
        this.userPoolId = userPoolId;
    }

    public void createUser(String email, String username, String password) {
        AttributeType emailAttr = new AttributeType().withName("email").withValue(email);
        AttributeType emailVerifiedAttr =
                new AttributeType().withName("email_verified").withValue("false");

        AdminCreateUserRequest userRequest = new AdminCreateUserRequest()
                .withUserPoolId(userPoolId)
                .withUsername(username)
                .withTemporaryPassword(password)
                .withUserAttributes(emailAttr, emailVerifiedAttr)
                .withMessageAction(MessageActionType.SUPPRESS);

        cognitoClient.adminCreateUser(userRequest);

        // Make the password permanent and not temporary
        AdminSetUserPasswordRequest adminSetUserPasswordRequest = new AdminSetUserPasswordRequest()
                .withUsername(username)
                .withUserPoolId(userPoolId)
                .withPassword(password)
                .withPermanent(true);
        cognitoClient.adminSetUserPassword(adminSetUserPasswordRequest);
    }
}

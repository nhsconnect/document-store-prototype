package uk.nhs.digital.docstore.testHarness.helpers;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.*;

public class UserFactory {
    private AWSCognitoIdentityProvider cognitoClient;
    private String userPoolId;

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

        AdminCreateUserResult createUserResult = cognitoClient.adminCreateUser(userRequest);

        System.out.println("User " + createUserResult.getUser().getUsername()
                + " is created. Status: " + createUserResult.getUser().getUserStatus());

        // Make the password permanent and not temporary
        AdminSetUserPasswordRequest adminSetUserPasswordRequest = new AdminSetUserPasswordRequest()
                .withUsername(username)
                .withUserPoolId(userPoolId)
                .withPassword(password)
                .withPermanent(true);
        cognitoClient.adminSetUserPassword(adminSetUserPasswordRequest);
    }
}

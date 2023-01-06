package uk.nhs.digital.docstore.patientdetails;

public class AccessTokenRequestBody {
    private final String clientAssertion;

    public AccessTokenRequestBody(String clientAssertion) {
        this.clientAssertion = clientAssertion;
    }

    public String toFormUrlEncodedString() {
        var requestBody = new StringBuilder();
        requestBody.append("grant_type=client_credentials");
        requestBody.append("client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
        requestBody.append("client_assertion=");
        requestBody.append(this.clientAssertion);
        return requestBody.toString();
    }
}

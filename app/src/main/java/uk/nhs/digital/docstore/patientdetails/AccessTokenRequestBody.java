package uk.nhs.digital.docstore.patientdetails;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.stream.Collectors;

public class AccessTokenRequestBody {
    private final String clientAssertion;

    public AccessTokenRequestBody(String clientAssertion) {
        this.clientAssertion = clientAssertion;
    }

    public String bodyToFormUrlEncodedString() {
        var parameters = new HashMap<String, String>();
        parameters.put("grant_type", "client_credentials");
        parameters.put(
                "client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
        parameters.put("client_assertion", this.clientAssertion);

        String urlEncoded =
                parameters.entrySet().stream()
                        .map(
                                e ->
                                        e.getKey()
                                                + "="
                                                + URLEncoder.encode(
                                                        e.getValue(), StandardCharsets.UTF_8))
                        .collect(Collectors.joining("&"));
        return urlEncoded;
    }
}

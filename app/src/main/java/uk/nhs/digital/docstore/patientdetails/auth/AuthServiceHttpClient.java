package uk.nhs.digital.docstore.patientdetails.auth;

import static java.net.http.HttpClient.newHttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import uk.nhs.digital.docstore.patientdetails.AccessTokenRequestBody;

public class AuthServiceHttpClient {
    public AccessToken fetchAccessToken(String signedJwt, String endpoint) {

        var accessTokenRequest =
                HttpRequest.newBuilder(URI.create(endpoint))
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        new AccessTokenRequestBody(signedJwt)
                                                .bodyToFormUrlEncodedString()))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .build();

        try {
            HttpResponse<String> response =
                    newHttpClient().send(accessTokenRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException(response.body());
            }
            return AccessToken.parse(response.body());

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

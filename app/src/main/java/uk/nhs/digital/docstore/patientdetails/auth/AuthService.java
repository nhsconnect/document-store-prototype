package uk.nhs.digital.docstore.patientdetails.auth;

public class AuthService {
    private final AuthServiceHttpClient httpClient;

    public AuthService(AuthServiceHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String getAccessToken() {
        return httpClient.fetchAccessToken().getAccessToken();
    }
}
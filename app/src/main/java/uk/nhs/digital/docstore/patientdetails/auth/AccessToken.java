package uk.nhs.digital.docstore.patientdetails.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AccessToken {
    private final String accessToken;
    private final String expiresIn;
    private final String tokenType;

    public AccessToken(@JsonProperty("access_token") String accessToken,
                       @JsonProperty("expires_in") String expiresIn,
                       @JsonProperty("token_type") String tokenType) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.tokenType = tokenType;
    }

    public static AccessToken parse(String json){
        var objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, AccessToken.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getAccessToken() {
        return accessToken;
    }
}

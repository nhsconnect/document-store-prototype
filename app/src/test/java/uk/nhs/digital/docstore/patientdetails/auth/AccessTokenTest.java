package uk.nhs.digital.docstore.patientdetails.auth;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccessTokenTest {

    @Test
    void parseFromJson() {
        var json = new JSONObject();
        json.put("access_token", "qwerty");

        var accessToken = AccessToken.parse(json.toString());

        assertThat(accessToken.getAccessToken()).isEqualTo(json.get("access_token"));
    }
}
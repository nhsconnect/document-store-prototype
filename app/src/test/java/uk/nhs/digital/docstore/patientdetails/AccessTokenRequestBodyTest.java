package uk.nhs.digital.docstore.patientdetails;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AccessTokenRequestBodyTest {

  @Test
  void returnAStringInFormUrlEncodedFormat() {
    var jwt = "test-jwt";
    var expectedBody =
        "client_assertion_type=urn%3Aietf%3Aparams%3Aoauth%3Aclient-assertion-type%3Ajwt-bearer&grant_type=client_credentials&client_assertion="
            + jwt;
    var body = new AccessTokenRequestBody(jwt).bodyToFormUrlEncodedString();
    assertThat(body).isEqualTo(expectedBody);
  }
}

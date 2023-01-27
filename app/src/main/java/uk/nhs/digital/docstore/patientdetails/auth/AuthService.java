package uk.nhs.digital.docstore.patientdetails.auth;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.PutParameterRequest;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.exceptions.MissingEnvironmentVariableException;
import uk.nhs.digital.docstore.patientdetails.PatientSearchConfig;

public class AuthService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
  private final AuthServiceHttpClient httpClient;
  private final PatientSearchConfig patientSearchConfig;
  private final SignedJwtBuilder jwtBuilder;
  private final AWSSimpleSystemsManagement ssm;

  public AuthService(AuthServiceHttpClient httpClient, PatientSearchConfig patientSearchConfig) {
    this(
        httpClient,
        patientSearchConfig,
        new SignedJwtBuilder(Clock.systemUTC(), patientSearchConfig),
        AWSSimpleSystemsManagementClientBuilder.standard().withRegion(Regions.EU_WEST_2).build());
  }

  public AuthService(
      AuthServiceHttpClient httpClient,
      PatientSearchConfig patientSearchConfig,
      SignedJwtBuilder jwtBuilder,
      AWSSimpleSystemsManagement ssm) {
    this.httpClient = httpClient;
    this.patientSearchConfig = patientSearchConfig;
    this.jwtBuilder = jwtBuilder;
    this.ssm = ssm;
  }

  public String retrieveAccessToken() {
    LOGGER.debug("Attempting to retrieve pdh fhir access token from parameter store");
    try {
      GetParameterRequest request = new GetParameterRequest();
      request.withName(patientSearchConfig.pdsFhirTokenName());
      request.withWithDecryption(true);

      GetParameterResult result = ssm.getParameter(request);
      LOGGER.debug("Successfully retrieved pdh fhir access token");
      return result.getParameter().getValue();

    } catch (AmazonServiceException | MissingEnvironmentVariableException e) {
      throw new RuntimeException(e);
    }
  }

  public String getNewAccessToken() {
    try {
      var signedJwt = jwtBuilder.build();
      LOGGER.debug("Fetching new access token from nhs");
      var accessTokenResponse =
          httpClient.fetchAccessToken(signedJwt, patientSearchConfig.nhsOauthEndpoint());

      PutParameterRequest request = new PutParameterRequest();
      request.withName(patientSearchConfig.pdsFhirTokenName());
      request.withValue(accessTokenResponse.getAccessToken());
      request.withOverwrite(true);
      LOGGER.debug("Attempting to update pdh fhir access token in parameter store");
      ssm.putParameter(request);
      LOGGER.debug("Successfully updated parameter store with new pdh fhir access token");
      return accessTokenResponse.getAccessToken();

    } catch (AmazonServiceException | MissingEnvironmentVariableException e) {
      throw new RuntimeException(e);
    }
  }
}

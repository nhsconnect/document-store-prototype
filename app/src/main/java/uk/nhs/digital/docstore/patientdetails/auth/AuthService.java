package uk.nhs.digital.docstore.patientdetails.auth;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.simplesystemsmanagement.model.PutParameterRequest;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.exceptions.MissingEnvironmentVariableException;
import uk.nhs.digital.docstore.patientdetails.PatientSearchConfig;
import uk.nhs.digital.docstore.utils.SSMService;

public class AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private final AuthServiceHttpClient httpClient;
    private final PatientSearchConfig patientSearchConfig;
    private final SignedJwtBuilder jwtBuilder;
    private final SSMService ssmService;

    public AuthService(
            AuthServiceHttpClient httpClient,
            PatientSearchConfig patientSearchConfig,
            SSMService ssmService) {
        this(
                httpClient,
                patientSearchConfig,
                new SignedJwtBuilder(Clock.systemUTC(), patientSearchConfig),
                new SSMService());
    }

    public AuthService(
            AuthServiceHttpClient httpClient,
            PatientSearchConfig patientSearchConfig,
            SignedJwtBuilder jwtBuilder,
            SSMService ssmService) {
        this.httpClient = httpClient;
        this.patientSearchConfig = patientSearchConfig;
        this.jwtBuilder = jwtBuilder;
        this.ssmService = ssmService;
    }

    public String retrieveAccessToken() {
        LOGGER.debug("Attempting to retrieve pdh fhir access token from parameter store");
        try {
            return ssmService.retrieveParameterStoreValue(patientSearchConfig.pdsFhirTokenName());
        } catch (MissingEnvironmentVariableException e) {
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
            ssmService.getClient().putParameter(request);
            LOGGER.debug("Successfully updated parameter store with new pdh fhir access token");
            return accessTokenResponse.getAccessToken();

        } catch (AmazonServiceException | MissingEnvironmentVariableException e) {
            throw new RuntimeException(e);
        }
    }
}

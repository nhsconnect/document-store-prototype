package uk.nhs.digital.docstore.patientdetails.auth;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.ParameterType;
import com.amazonaws.services.simplesystemsmanagement.model.PutParameterRequest;
import uk.nhs.digital.docstore.config.MissingEnvironmentVariableException;
import uk.nhs.digital.docstore.patientdetails.PatientSearchConfig;

import java.time.Clock;

public class AuthService {
    private final AuthServiceHttpClient httpClient;
    private final PatientSearchConfig patientSearchConfig;
    private final SignedJwtBuilder jwtBuilder;
    private final AWSSimpleSystemsManagement ssm;

    public AuthService(AuthServiceHttpClient httpClient, PatientSearchConfig patientSearchConfig) {
        this(httpClient, patientSearchConfig, new SignedJwtBuilder(Clock.systemUTC(), patientSearchConfig),
                AWSSimpleSystemsManagementClientBuilder.standard().withRegion(Regions.EU_WEST_2).build());
    }

    public AuthService(AuthServiceHttpClient httpClient, PatientSearchConfig patientSearchConfig, SignedJwtBuilder jwtBuilder, AWSSimpleSystemsManagement ssm) {
        this.httpClient = httpClient;
        this.patientSearchConfig = patientSearchConfig;
        this.jwtBuilder = jwtBuilder;
        this.ssm = ssm;
    }

    public String retrieveAccessToken() {
        try {
            GetParameterRequest request = new GetParameterRequest();
            request.withName(patientSearchConfig.pdsFhirTokenName());
            request.withWithDecryption(true);

            GetParameterResult result = ssm.getParameter(request);

            return result.getParameter().getValue();

        } catch (AmazonServiceException | MissingEnvironmentVariableException e) {
            throw new RuntimeException(e);
        }
    }

    public String getNewAccessToken() {
        try {
            var signedJwt = jwtBuilder.build();
            var accessTokenResponse = httpClient.fetchAccessToken(signedJwt, patientSearchConfig.nhsOauthEndpoint());

            PutParameterRequest request = new PutParameterRequest();
            request.withName(patientSearchConfig.pdsFhirTokenName());
            request.withType(ParameterType.SecureString);
            request.withOverwrite(true);

            ssm.putParameter(request);

            return accessTokenResponse.getAccessToken();

        } catch (AmazonServiceException | MissingEnvironmentVariableException e) {
            throw new RuntimeException(e);
        }
    }
}
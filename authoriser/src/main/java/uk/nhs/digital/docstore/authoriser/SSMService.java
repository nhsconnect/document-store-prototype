package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSMService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SSMService.class);
    private static AWSSimpleSystemsManagement ssmClient;

    public static AWSSimpleSystemsManagement retrieveSsmClient() {
        if (ssmClient == null) {
            ssmClient =
                    AWSSimpleSystemsManagementClientBuilder.standard()
                            .withRegion(Regions.EU_WEST_2)
                            .build();
        }
        return ssmClient;
    }

    public static String retrieveParameterStoreValue(String ssmParam) {
        ssmClient = retrieveSsmClient();
        try {
            LOGGER.debug("Attempting to retrieve value from parameter store using: {}", ssmParam);
            GetParameterRequest request = new GetParameterRequest();
            request.withName(ssmParam);
            request.withWithDecryption(true);

            GetParameterResult result = ssmClient.getParameter(request);
            LOGGER.debug("Successfully retrieved value");
            return result.getParameter().getValue();

        } catch (AmazonServiceException e) {
            throw new RuntimeException(e);
        }
    }
}

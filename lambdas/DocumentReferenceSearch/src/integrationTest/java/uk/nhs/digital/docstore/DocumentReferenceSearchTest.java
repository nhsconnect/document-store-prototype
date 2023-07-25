package uk.nhs.digital.docstore;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.jayway.jsonpath.JsonPath;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.helpers.DocumentMetadataBuilder;
import uk.nhs.digital.docstore.lambdas.DocumentReferenceSearchHandler;
import uk.nhs.digital.docstore.model.NhsNumber;
import uk.nhs.digital.docstore.services.DocumentMetadataSearchService;

@ExtendWith(MockitoExtension.class)
public class DocumentReferenceSearchTest extends BaseDocumentStoreTest {
    private DocumentMetadataStore metadataStore;
    private DocumentReferenceSearchHandler handler;
    @Mock private Context context;

    @BeforeEach
    void setUp() {
        metadataStore = new DocumentMetadataStore(dynamoDBHelper.getMapper());
        handler =
                new DocumentReferenceSearchHandler(
                        new DocumentMetadataSearchService(
                                metadataStore, new DocumentMetadataSerialiser()));
    }

    @Test
    public void returnsAResourceBundleForTheGivenNhsNumber()
            throws IllFormedPatientDetailsException {
        var nhsNumber = new NhsNumber("1000000009");
        var uploadedMetadataBuilder =
                DocumentMetadataBuilder.theMetadata()
                        .withNhsNumber(nhsNumber)
                        .withDocumentUploaded(true)
                        .withIndexed(Instant.now().toString());
        var nonUploadedMetadata =
                DocumentMetadataBuilder.theMetadata().withNhsNumber(nhsNumber).build();
        var metadataItems =
                List.of(
                        uploadedMetadataBuilder.build(),
                        uploadedMetadataBuilder.build(),
                        nonUploadedMetadata);

        metadataItems.forEach(
                (metadataItem) -> {
                    metadataStore.save(metadataItem);
                });

        var response = handler.handleRequest(createRequestEvent(nhsNumber), context);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getHeaders().get("Content-Type")).contains("application/fhir+json");

        var responseBody = response.getBody();

        var count = JsonPath.read(responseBody, "$.total");
        assertThat(count).isEqualTo(2);

        List<String> nhsNumbers =
                JsonPath.read(responseBody, "$.entry[*].resource.subject.identifier.value");
        assertThat(nhsNumbers.get(0)).isEqualTo(nhsNumber.getValue());
        assertThat(nhsNumbers.get(1)).isEqualTo(nhsNumber.getValue());

        List<String> documentTitles =
                JsonPath.read(responseBody, "$.entry[*].resource.description");
        assertThat(documentTitles).contains(metadataItems.get(0).getFileName());
        assertThat(documentTitles).contains(metadataItems.get(1).getFileName());
    }

    private APIGatewayProxyRequestEvent createRequestEvent(NhsNumber nhsNumber) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(
                "subject:identifier", "https://fhir.nhs.uk/Id/nhs-number|" + nhsNumber.getValue());

        return new APIGatewayProxyRequestEvent().withQueryStringParameters(parameters);
    }
}

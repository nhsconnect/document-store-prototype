package uk.nhs.digital.docstore;

import ca.uhn.fhir.context.FhirContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ca.uhn.fhir.context.PerformanceOptionsEnum.DEFERRED_MODEL_SCANNING;
import static java.util.stream.Collectors.toList;
import static org.hl7.fhir.r4.model.Bundle.BundleType.SEARCHSET;
import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.FINAL;
import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.PRELIMINARY;

@SuppressWarnings("unused")
public class DocumentReferenceSearchHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Pattern SUBJECT_IDENTIFIER_PATTERN = Pattern.compile("^(?:(?<system>.*?)(?<!\\\\)\\|)?(?<identifier>.*)$");

    private final DocumentMetadataStore metadataStore = new DocumentMetadataStore();
    private final DocumentStore documentStore = new DocumentStore(System.getenv("DOCUMENT_STORE_BUCKET_NAME"));
    private final FhirContext fhirContext;

    public DocumentReferenceSearchHandler() {
        this.fhirContext = FhirContext.forR4();
        this.fhirContext.setPerformanceOptions(DEFERRED_MODEL_SCANNING);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        var jsonParser = fhirContext.newJsonParser();

        String nhsNumber = getNhsNumberFrom(requestEvent.getQueryStringParameters());
        List<DocumentMetadata> metadata = metadataStore.findByNhsNumber(nhsNumber);
        List<BundleEntryComponent> entries = metadata.stream()
                .map(this::toBundleEntries)
                .collect(toList());

        var bundle = new Bundle()
                .setTotal(entries.size())
                .setType(SEARCHSET)
                .setEntry(entries);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(Map.of("Content-Type", "application/fhir+json"))
                .withBody(jsonParser.encodeResourceToString(bundle));
    }

    private String getNhsNumberFrom(Map<String, String> queryParameters) {
        String subject = Optional.ofNullable(queryParameters.get("subject:identifier"))
                .or(() -> Optional.ofNullable(queryParameters.get("subject.identifier")))
                .orElseThrow();
        Matcher matcher = SUBJECT_IDENTIFIER_PATTERN.matcher(subject);
        if (matcher.matches()) {
            return matcher.group("identifier");
        }
        throw new RuntimeException();
    }

    private BundleEntryComponent toBundleEntries(DocumentMetadata metadata) {
        return new BundleEntryComponent()
                .setFullUrl("/DocumentReference/" + metadata.getId())
                .setResource(toDocumentReference(metadata));
    }

    private DocumentReference toDocumentReference(DocumentMetadata metadata) {
        DocumentReference.DocumentReferenceContentComponent contentComponent = null;
        if (metadata.isDocumentUploaded()) {
            URL preSignedUrl = documentStore.generatePreSignedUrl(DocumentStore.DocumentDescriptor.from(metadata));
            contentComponent = new DocumentReference.DocumentReferenceContentComponent()
                    .setAttachment(new Attachment()
                            .setContentType(metadata.getContentType())
                            .setUrl(preSignedUrl.toExternalForm()));
        }

        return (DocumentReference) new DocumentReference()
                .setSubject(new Reference()
                        .setIdentifier(new Identifier()
                                .setSystem("https://fhir.nhs.uk/Id/nhs-number")
                                .setValue(metadata.getNhsNumber())))
                .addContent(contentComponent)
                .setDocStatus(metadata.isDocumentUploaded() ? FINAL : PRELIMINARY)
                .setId(metadata.getId());
    }
}

package uk.nhs.digital.docstore;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.PerformanceOptionsEnum;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.DocumentStore.DocumentDescriptor;

import java.util.Map;

import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.FINAL;
import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.PRELIMINARY;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.NOTFOUND;

@SuppressWarnings("unused")
public class RetrieveDocumentReferenceHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DocumentMetadataStore metadataStore = new DocumentMetadataStore();
    private final DocumentStore documentStore = new DocumentStore(System.getenv("DOCUMENT_STORE_BUCKET_NAME"));
    private final FhirContext fhirContext;

    private static final Logger logger
            = LoggerFactory.getLogger(RetrieveDocumentReferenceHandler.class);

    public RetrieveDocumentReferenceHandler() {
        this.fhirContext = FhirContext.forR4();
        this.fhirContext.setPerformanceOptions(PerformanceOptionsEnum.DEFERRED_MODEL_SCANNING);
    }

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        logger.debug("API Gateway event received - processing starts");
        var jsonParser = fhirContext.newJsonParser();

        logger.debug("Processing - after loading fhir context");
        var metadata = metadataStore.getById(event.getPathParameters().get("id"));
        logger.debug("API Gateway event received - processing starts");

        if (metadata == null) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(404)
                    .withHeaders(Map.of("Content-Type", "application/fhir+json"))
                    .withBody(jsonParser.encodeResourceToString(new OperationOutcome()
                            .addIssue(new OperationOutcome.OperationOutcomeIssueComponent()
                                    .setSeverity(ERROR)
                                    .setCode(NOTFOUND)
                                    .setDetails(new CodeableConcept()
                                            .addCoding(new Coding()
                                                    .setSystem("https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1")
                                                    .setCode("NO_RECORD_FOUND")
                                                    .setDisplay("No record found"))))));
        }

        logger.debug("Retrieved the requested object. Creating the pre-signed URL");
        var preSignedUri = documentStore.generatePreSignedUrl(DocumentDescriptor.from(metadata));

        logger.debug("Created the pre-signed URL - about to transform it into JSON");
        var resource = new DocumentReference()
                .setSubject(new Reference()
                        .setIdentifier(new Identifier()
                                .setSystem("https://fhir.nhs.uk/Id/nhs-number")
                                .setValue(metadata.getNhsNumber())))
                .addContent(new DocumentReference.DocumentReferenceContentComponent()
                        .setAttachment(new Attachment()
                                .setUrl(preSignedUri.toString())
                                .setContentType(metadata.getContentType())))
                .setDocStatus(metadata.isDocumentUploaded() ? FINAL : PRELIMINARY)
                .setId(metadata.getId());
        var resourceAsJson = new SimpleJsonEncoder().encodeDocumentReferenceToString(metadata, preSignedUri);

        logger.debug("Processing finished - about to return the response");
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(Map.of("Content-Type", "application/fhir+json"))
                .withBody(resourceAsJson);
    }
}

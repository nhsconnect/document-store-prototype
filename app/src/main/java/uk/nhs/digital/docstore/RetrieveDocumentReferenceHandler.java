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
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.DocumentStore.DocumentDescriptor;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.config.Tracer;

import java.net.URL;

import static java.util.stream.Collectors.toList;
import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.FINAL;
import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.PRELIMINARY;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.NOTFOUND;

@SuppressWarnings("unused")
public class RetrieveDocumentReferenceHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final String DOCUMENT_TYPE_CODING_SYSTEM = "http://snomed.info/sct";
    private final DocumentMetadataStore metadataStore = new DocumentMetadataStore();
    private final DocumentStore documentStore = new DocumentStore(System.getenv("DOCUMENT_STORE_BUCKET_NAME"));
    private final FhirContext fhirContext;
    private final ApiConfig apiConfig;

    private static final Logger logger
            = LoggerFactory.getLogger(RetrieveDocumentReferenceHandler.class);

    public RetrieveDocumentReferenceHandler() {
        this(new ApiConfig());
    }

    public RetrieveDocumentReferenceHandler(ApiConfig apiConfig) {
        this.fhirContext = FhirContext.forR4();
        this.fhirContext.setPerformanceOptions(PerformanceOptionsEnum.DEFERRED_MODEL_SCANNING);
        this.apiConfig = apiConfig;
    }

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {

        Tracer.setMDCContext(context);

        logger.debug("API Gateway event received - processing starts");
        var jsonParser = fhirContext.newJsonParser();

        logger.debug("Processing - after loading fhir context");
        var metadata = metadataStore.getById(event.getPathParameters().get("id"));
        logger.debug("API Gateway event received - processing starts");

        if (metadata == null) {
            var body = jsonParser.encodeResourceToString(new OperationOutcome()
                    .addIssue(new OperationOutcome.OperationOutcomeIssueComponent()
                            .setSeverity(ERROR)
                            .setCode(NOTFOUND)
                            .setDetails(new CodeableConcept()
                                    .addCoding(new Coding()
                                            .setSystem("https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1")
                                            .setCode("NO_RECORD_FOUND")
                                            .setDisplay("No record found")))));
            return apiConfig.getApiGatewayResponse(404, body,"GET", null);
        }

        DocumentReferenceContentComponent contentComponent = null;
        URL preSignedUrl;
        if (metadata.isDocumentUploaded()) {
            logger.debug("Retrieved the requested object. Creating the pre-signed URL");
            preSignedUrl = documentStore.generatePreSignedUrl(DocumentDescriptor.from(metadata));
            contentComponent = new DocumentReferenceContentComponent()
                    .setAttachment(new Attachment()
                            .setUrl(preSignedUrl.toString())
                            .setContentType(metadata.getContentType()));
        } else {
            logger.debug("Skipping pre-signed URL: it's not been uploaded yet.");
        }

        logger.debug("About to transform response into JSON");
        var type = new CodeableConcept()
                .setCoding(metadata.getType()
                        .stream()
                        .map(code -> new Coding()
                                .setCode(code)
                                .setSystem(DOCUMENT_TYPE_CODING_SYSTEM))
                        .collect(toList()));
        var resource = new NHSDocumentReference()
                .setCreated(new DateTimeType(metadata.getCreated()))
                .setIndexed(new InstantType(metadata.getIndexed()))
                .setSubject(new Reference()
                        .setIdentifier(new Identifier()
                                .setSystem("https://fhir.nhs.uk/Id/nhs-number")
                                .setValue(metadata.getNhsNumber())))
                .addContent(contentComponent)
                .setType(type)
                .setDocStatus(metadata.isDocumentUploaded() ? FINAL : PRELIMINARY)
                .setDescription(metadata.getDescription())
                .setId(metadata.getId());

        var resourceAsJson = jsonParser.encodeResourceToString(resource);

        logger.debug("Processing finished - about to return the response");
        return apiConfig.getApiGatewayResponse(200, resourceAsJson, "GET", null);
    }
}

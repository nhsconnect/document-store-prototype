package uk.nhs.digital.docstore;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.PerformanceOptionsEnum;
import ca.uhn.fhir.parser.IParser;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.DocumentStore.DocumentDescriptor;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.config.Tracer;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.exceptions.IllFormedPatentDetailsException;
import uk.nhs.digital.docstore.model.NhsNumber;

import static java.util.stream.Collectors.toList;
import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.FINAL;
import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.PRELIMINARY;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.NOTFOUND;

@SuppressWarnings("unused")
public class RetrieveDocumentReferenceHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger = LoggerFactory.getLogger(RetrieveDocumentReferenceHandler.class);
    private static final String DOCUMENT_TYPE_CODING_SYSTEM = "http://snomed.info/sct";
    private final DocumentMetadataStore metadataStore = new DocumentMetadataStore();
    private final DocumentStore documentStore = new DocumentStore(System.getenv("DOCUMENT_STORE_BUCKET_NAME"));
    private final ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();
    private final FhirContext fhirContext;
    private final ApiConfig apiConfig;

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

        try {
            var jsonParser = fhirContext.newJsonParser();

            logger.debug("Processing - after loading fhir context");
            var metadata = metadataStore.getById(event.getPathParameters().get("id"));

            logger.debug("API Gateway event received - processing starts");

            if (metadata == null) {
                var body = get404ResponseBody(jsonParser);
                return apiConfig.getApiGatewayResponse(404, body,"GET", null);
            }

            var content = getContentWithPresignedUrl(metadata);

            logger.debug("About to transform response into JSON");
            var resource = createResourceFromMetadata(metadata, content);

            var body = jsonParser.encodeResourceToString(resource);

            logger.debug("Processing finished - about to return the response");
            return apiConfig.getApiGatewayResponse(200, body, "GET", null);
        }
            catch (Exception e) {
            return errorResponseGenerator.errorResponse(e);
        }
    }

    private String get404ResponseBody(IParser jsonParser) {
        return jsonParser.encodeResourceToString(new OperationOutcome()
                .addIssue(new OperationOutcome.OperationOutcomeIssueComponent()
                        .setSeverity(ERROR)
                        .setCode(NOTFOUND)
                        .setDetails(new CodeableConcept()
                                .addCoding(new Coding()
                                        .setSystem("https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1")
                                        .setCode("NO_RECORD_FOUND")
                                        .setDisplay("No record found")))));
    }

    private DocumentReferenceContentComponent getContentWithPresignedUrl(DocumentMetadata metadata) {
        if (!metadata.isDocumentUploaded()) {
            logger.debug("Skipping pre-signed URL: it's not been uploaded yet.");
            return null;
        }

        logger.debug("Retrieved the requested object. Creating the pre-signed URL");
        var preSignedUrl = documentStore.generatePreSignedUrl(DocumentDescriptor.from(metadata));
        return new DocumentReferenceContentComponent()
                .setAttachment(new Attachment()
                        .setUrl(preSignedUrl.toString())
                        .setContentType(metadata.getContentType()));
    }

    private Resource createResourceFromMetadata(DocumentMetadata metadata, DocumentReferenceContentComponent content) throws IllFormedPatentDetailsException {
        var type = new CodeableConcept()
                .setCoding(metadata.getType()
                        .stream()
                        .map(code -> new Coding()
                                .setCode(code)
                                .setSystem(DOCUMENT_TYPE_CODING_SYSTEM))
                        .collect(toList()));

        return new NHSDocumentReference()
                .setCreated(new DateTimeType(metadata.getCreated()))
                .setIndexed(new InstantType(metadata.getIndexed()))
                .setNhsNumber(new NhsNumber(metadata.getNhsNumber()))
                .addContent(content)
                .setType(type)
                .setDocStatus(metadata.isDocumentUploaded() ? FINAL : PRELIMINARY)
                .setDescription(metadata.getDescription())
                .setId(metadata.getId());
    }
}

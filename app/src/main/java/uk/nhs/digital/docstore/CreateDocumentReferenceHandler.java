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
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.DocumentStore.DocumentDescriptorAndURL;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.config.Tracer;
import uk.nhs.digital.docstore.create.CreateDocumentReferenceRequestValidator;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;

import static java.util.stream.Collectors.toList;
import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.FINAL;
import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.PRELIMINARY;

@SuppressWarnings("unused")
public class CreateDocumentReferenceHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger
            = LoggerFactory.getLogger(CreateDocumentReferenceHandler.class);
    private static final String DOCUMENT_TYPE_CODING_SYSTEM = "http://snomed.info/sct";
    private static final String SUBJECT_ID_CODING_SYSTEM = "https://fhir.nhs.uk/Id/nhs-number";

    private final DocumentMetadataStore metadataStore = new DocumentMetadataStore();
    private final DocumentStore documentStore = new DocumentStore(System.getenv("DOCUMENT_STORE_BUCKET_NAME"));
    private final ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();
    private final FhirContext fhirContext;
    private final CreateDocumentReferenceRequestValidator requestValidator = new CreateDocumentReferenceRequestValidator();
    private final ApiConfig apiConfig;

    public CreateDocumentReferenceHandler() {
        this(new ApiConfig());
    }

    public CreateDocumentReferenceHandler(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
        this.fhirContext = FhirContext.forR4();
        this.fhirContext.setPerformanceOptions(PerformanceOptionsEnum.DEFERRED_MODEL_SCANNING);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

        Tracer.setMDCContext(context);

        logger.debug("API Gateway event received - processing starts");
        var jsonParser = fhirContext.newJsonParser();

        DocumentMetadata savedDocumentMetadata;
        DocumentDescriptorAndURL documentDescriptorAndURL;
        try {
            var inputDocumentReference = jsonParser.parseResource(NHSDocumentReference.class, input.getBody());
            requestValidator.validate(inputDocumentReference);

            logger.debug("Saving DocumentReference to DynamoDB");
            documentDescriptorAndURL = documentStore.generateDocumentDescriptorAndURL();
            var documentMetadata = DocumentMetadata.from(inputDocumentReference, documentDescriptorAndURL.getDocumentDescriptor());
            savedDocumentMetadata = metadataStore.save(documentMetadata);
        } catch (Exception e) {
            return errorResponseGenerator.errorResponse(e, jsonParser);
        }

        logger.debug("Generating response body");
        var type = new CodeableConcept()
                .setCoding(savedDocumentMetadata.getType()
                        .stream()
                        .map(code -> new Coding()
                                .setCode(code)
                                .setSystem(DOCUMENT_TYPE_CODING_SYSTEM))
                        .collect(toList()));

        var resource = new NHSDocumentReference()
                .setCreated(new DateTimeType(savedDocumentMetadata.getCreated()))
                .setSubject(new Reference()
                        .setIdentifier(new Identifier()
                                .setSystem(SUBJECT_ID_CODING_SYSTEM)
                                .setValue(savedDocumentMetadata.getNhsNumber())))
                .addContent(new NHSDocumentReference.DocumentReferenceContentComponent()
                        .setAttachment(new Attachment()
                                .setUrl(documentDescriptorAndURL.getDocumentUrl())
                                .setContentType(savedDocumentMetadata.getContentType())))
                .setType(type)
                .setDocStatus(savedDocumentMetadata.isDocumentUploaded() ? FINAL : PRELIMINARY)
                .setDescription(savedDocumentMetadata.getDescription())
                .setId(savedDocumentMetadata.getId());
        var resourceAsJson = jsonParser.encodeResourceToString(resource);

        logger.debug("Processing finished - about to return the response");
        return apiConfig.getApiGatewayResponse(201, resourceAsJson, "POST", "DocumentReference/" + savedDocumentMetadata.getId());
    }
}

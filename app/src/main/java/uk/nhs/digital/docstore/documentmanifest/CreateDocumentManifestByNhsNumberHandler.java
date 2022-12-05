package uk.nhs.digital.docstore.documentmanifest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.PerformanceOptionsEnum;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import uk.nhs.digital.docstore.DocumentStore;
import uk.nhs.digital.docstore.ErrorResponseGenerator;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.config.Tracer;
import uk.nhs.digital.docstore.data.entity.DocumentZipTrace;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.repository.DocumentZipTraceStore;
import uk.nhs.digital.docstore.utils.CommonUtils;
import uk.nhs.digital.docstore.utils.DocumentMetadataSearchService;
import uk.nhs.digital.docstore.utils.ZipService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;


@SuppressWarnings("unused")
public class CreateDocumentManifestByNhsNumberHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger = LoggerFactory.getLogger(CreateDocumentManifestByNhsNumberHandler.class);
    private static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");
    private static final String SUBJECT_ID_CODING_SYSTEM = "https://fhir.nhs.uk/Id/nhs-number";
    private static final String DOCUMENT_TYPE_CODING_SYSTEM = "http://snomed.info/sct";

    private final String bucketName = System.getenv("DOCUMENT_STORE_BUCKET_NAME");
    private final DocumentMetadataStore metadataStore = new DocumentMetadataStore();
    private final DocumentZipTraceStore zipTraceStore = new DocumentZipTraceStore();
    private final DocumentStore documentStore = new DocumentStore(bucketName);
    private final ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();
    private final FhirContext fhirContext;
    private final ApiConfig apiConfig;
    private final DocumentMetadataSearchService searchService = new DocumentMetadataSearchService(metadataStore);
    private final CommonUtils utils = new CommonUtils();
    private final ZipService zipService = new ZipService();

    public CreateDocumentManifestByNhsNumberHandler() {
        this(new ApiConfig());
    }

    public CreateDocumentManifestByNhsNumberHandler(ApiConfig apiConfig) {
        this.fhirContext = FhirContext.forR4();
        this.fhirContext.setPerformanceOptions(PerformanceOptionsEnum.DEFERRED_MODEL_SCANNING);
        this.apiConfig = apiConfig;
    }

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        Tracer.setMDCContext(context);

        logger.debug("API Gateway event received - processing starts");

        try {
            var nhsNumber = utils.getNhsNumberFrom(requestEvent.getQueryStringParameters());

            var documentMetadataList = searchService.findMetadataByNhsNumber(nhsNumber, requestEvent.getHeaders());

            var zipInputStream = zipService.zipDocuments(documentMetadataList);

            var documentPath = "tmp/" + CommonUtils.generateRandomUUIDString();
            var fileName = "patient-record-" + nhsNumber + ".zip";

            documentStore.addDocument(documentPath, zipInputStream);

            var descriptor = new DocumentStore.DocumentDescriptor(bucketName, documentPath);

            zipTraceStore.save(getDocumentZipTrace(descriptor.toLocation()));

            var preSignedUrl = documentStore.generatePreSignedUrlForZip(descriptor, fileName);
            var body = getJsonBody(preSignedUrl.toString());

            return apiConfig.getApiGatewayResponse(200, body, "GET", null);
        } catch (Exception e) {
            return errorResponseGenerator.errorResponse(e, fhirContext.newJsonParser());
        } catch (OutOfMemoryError outOfMemoryError) {
            return errorResponseGenerator.outOfMemoryResponse(outOfMemoryError);
        }
    }

    private DocumentZipTrace getDocumentZipTrace(String location) {
        long timeToLive = Long.parseLong(System.getenv("DOCUMENT_ZIP_TRACE_TTL_IN_DAYS"));
        var expDate = Instant.now().plus(timeToLive, ChronoUnit.SECONDS);
        var documentZipTrace = new DocumentZipTrace();
        documentZipTrace.setCorrelationId(Tracer.getCorrelationId());
        documentZipTrace.setCreatedAt(Instant.now().toString());
        documentZipTrace.setLocation(location);
        documentZipTrace.setExpiryDate(expDate.getEpochSecond());
        return documentZipTrace;
    }

    private String getJsonBody(String contents) {
        return "{\n" +
                "   \"result\": {\n" +
                "       \"url\": \"" + contents + "\"\n" +
                "   }\n" +
                "}";
    }
}

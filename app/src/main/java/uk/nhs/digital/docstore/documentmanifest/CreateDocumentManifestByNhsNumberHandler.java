package uk.nhs.digital.docstore.documentmanifest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.PerformanceOptionsEnum;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.DocumentStore;
import uk.nhs.digital.docstore.ErrorResponseGenerator;
import uk.nhs.digital.docstore.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.config.Tracer;
import uk.nhs.digital.docstore.data.entity.DocumentZipTrace;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.repository.DocumentZipTraceStore;
import uk.nhs.digital.docstore.services.DocumentManifestService;
import uk.nhs.digital.docstore.services.DocumentMetadataSearchService;
import uk.nhs.digital.docstore.utils.CommonUtils;
import uk.nhs.digital.docstore.utils.ZipService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class CreateDocumentManifestByNhsNumberHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateDocumentManifestByNhsNumberHandler.class);

    private final ApiConfig apiConfig;
    private final DocumentZipTraceStore zipTraceStore;
    private final DocumentStore documentStore;
    private final DocumentMetadataSearchService metadataSearchService;
    private final DocumentManifestService documentManifestService;
    private final ZipService zipService;
    private final String dbTimeToLive;

    private final ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();
    private final CommonUtils utils = new CommonUtils();

    @SuppressWarnings("unused")
    public CreateDocumentManifestByNhsNumberHandler() {
        this(
                new ApiConfig(),
                new DocumentMetadataStore(),
                new DocumentZipTraceStore(),
                new DocumentStore(System.getenv("DOCUMENT_STORE_BUCKET_NAME")),
                new DocumentManifestService(new SplunkPublisher()),
                System.getenv("DOCUMENT_ZIP_TRACE_TTL_IN_DAYS")
        );
    }

    public CreateDocumentManifestByNhsNumberHandler(ApiConfig apiConfig,
                                                    DocumentMetadataStore metadataStore,
                                                    DocumentZipTraceStore zipTraceStore,
                                                    DocumentStore documentStore,
                                                    DocumentManifestService documentManifestService,
                                                    String dbTimeToLive) {
        FhirContext fhirContext = FhirContext.forR4();
        fhirContext.setPerformanceOptions(PerformanceOptionsEnum.DEFERRED_MODEL_SCANNING);

        this.apiConfig = apiConfig;
        this.zipTraceStore = zipTraceStore;
        this.documentStore = documentStore;
        this.documentManifestService = documentManifestService;
        this.dbTimeToLive = dbTimeToLive;

        metadataSearchService = new DocumentMetadataSearchService(metadataStore);
        zipService = new ZipService(documentStore);
    }

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        Tracer.setMDCContext(context);
        LOGGER.debug("API Gateway event received - processing starts");

        try {
            var nhsNumber = utils.getNhsNumberFrom(requestEvent.getQueryStringParameters());
            var documentMetadataList = metadataSearchService.findMetadataByNhsNumber(
                    nhsNumber,
                    requestEvent.getHeaders()
            );
            var documentPath = "tmp/" + CommonUtils.generateRandomUUIDString();
            var fileName = "patient-record-" + nhsNumber + ".zip";

            var zipInputStream = zipService.zipDocuments(documentMetadataList);
            documentStore.addDocument(documentPath, zipInputStream);
            var descriptor = new DocumentStore.DocumentDescriptor(System.getenv("DOCUMENT_STORE_BUCKET_NAME"), documentPath);
            zipTraceStore.save(getDocumentZipTrace(descriptor.toLocation()));
            var preSignedUrl = documentStore.generatePreSignedUrlForZip(descriptor, fileName);
            documentManifestService.audit(nhsNumber, documentMetadataList);
            var responseBody = getJsonBody(preSignedUrl.toString());

            return apiConfig.getApiGatewayResponse(200, responseBody, "GET", null);
        } catch (Exception exception) {
            return errorResponseGenerator.errorResponse(exception);
        } catch (OutOfMemoryError outOfMemoryError) {
            return errorResponseGenerator.outOfMemoryResponse(outOfMemoryError);
        }
    }

    private DocumentZipTrace getDocumentZipTrace(String location) {
        long timeToLive = Long.parseLong(dbTimeToLive);
        var expDate = Instant.now().plus(timeToLive, ChronoUnit.DAYS);
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

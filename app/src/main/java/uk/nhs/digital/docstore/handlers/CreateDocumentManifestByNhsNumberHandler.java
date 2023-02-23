package uk.nhs.digital.docstore.handlers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.PerformanceOptionsEnum;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.ErrorResponseGenerator;
import uk.nhs.digital.docstore.NHSNumberSearchParameterForm;
import uk.nhs.digital.docstore.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.config.Tracer;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.repository.DocumentStore;
import uk.nhs.digital.docstore.data.repository.DocumentZipTraceStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.services.DocumentManifestService;
import uk.nhs.digital.docstore.services.DocumentMetadataSearchService;
import uk.nhs.digital.docstore.utils.ZipService;

public class CreateDocumentManifestByNhsNumberHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(CreateDocumentManifestByNhsNumberHandler.class);

    private final ApiConfig apiConfig;
    private final DocumentMetadataSearchService metadataSearchService;
    private final DocumentManifestService documentManifestService;
    private final ZipService zipService;
    private final ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();

    @SuppressWarnings("unused")
    public CreateDocumentManifestByNhsNumberHandler() {
        this(
                new ApiConfig(),
                new DocumentMetadataStore(),
                new DocumentZipTraceStore(),
                new DocumentStore(System.getenv("DOCUMENT_STORE_BUCKET_NAME")),
                new SplunkPublisher(System.getenv("SQS_AUDIT_QUEUE_URL")),
                System.getenv("DOCUMENT_ZIP_TRACE_TTL_IN_DAYS"));
    }

    public CreateDocumentManifestByNhsNumberHandler(
            ApiConfig apiConfig,
            DocumentMetadataStore metadataStore,
            DocumentZipTraceStore zipTraceStore,
            DocumentStore documentStore,
            SplunkPublisher splunkPublisher,
            String dbTimeToLive) {
        FhirContext fhirContext = FhirContext.forR4();
        fhirContext.setPerformanceOptions(PerformanceOptionsEnum.DEFERRED_MODEL_SCANNING);

        this.apiConfig = apiConfig;
        documentManifestService =
                new DocumentManifestService(
                        splunkPublisher, zipTraceStore, documentStore, dbTimeToLive);
        metadataSearchService =
                new DocumentMetadataSearchService(metadataStore, new DocumentMetadataSerialiser());
        zipService = new ZipService(documentStore);
    }

    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent requestEvent, Context context) {
        Tracer.setMDCContext(context);
        LOGGER.debug("API Gateway event received - processing starts");

        try {
            var nhsNumberSearchParameterForm =
                    new NHSNumberSearchParameterForm(requestEvent.getQueryStringParameters());
            var nhsNumber = nhsNumberSearchParameterForm.getNhsNumber();
            var documentMetadataList = metadataSearchService.findMetadataByNhsNumber(nhsNumber);
            var zipInputStream = zipService.zipDocuments(documentMetadataList);

            var presignedUrl =
                    documentManifestService.saveZip(
                            zipInputStream, documentMetadataList, nhsNumber);

            var responseBody = getJsonBody(presignedUrl);
            return apiConfig.getApiGatewayResponse(200, responseBody, "GET", null);
        } catch (Exception exception) {
            return errorResponseGenerator.errorResponse(exception);
        } catch (OutOfMemoryError outOfMemoryError) {
            return errorResponseGenerator.outOfMemoryResponse(outOfMemoryError);
        }
    }

    private String getJsonBody(String contents) {
        return "{\n"
                + "   \"result\": {\n"
                + "       \"url\": \""
                + contents
                + "\"\n"
                + "   }\n"
                + "}";
    }
}

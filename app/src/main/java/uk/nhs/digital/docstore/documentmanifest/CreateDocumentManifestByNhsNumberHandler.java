package uk.nhs.digital.docstore.documentmanifest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.PerformanceOptionsEnum;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import uk.nhs.digital.docstore.DocumentMetadata;
import uk.nhs.digital.docstore.DocumentMetadataStore;
import uk.nhs.digital.docstore.DocumentStore;
import uk.nhs.digital.docstore.ErrorResponseGenerator;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.config.Tracer;
import uk.nhs.digital.docstore.common.DocumentMetadataSearchService;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("unused")
public class CreateDocumentManifestByNhsNumberHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger = LoggerFactory.getLogger(CreateDocumentManifestByNhsNumberHandler.class);
    private static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");

    private static final String DOCUMENT_TYPE_CODING_SYSTEM = "http://snomed.info/sct";
    private final DocumentMetadataStore metadataStore = new DocumentMetadataStore();
    private final DocumentStore documentStore = new DocumentStore(System.getenv("DOCUMENT_STORE_BUCKET_NAME"));
    private final ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();
    private final FhirContext fhirContext;
    private final ApiConfig apiConfig;
    private final DocumentMetadataSearchService searchService = new DocumentMetadataSearchService(metadataStore);

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
        String userEmail = getEmail(requestEvent);

        try {
            Map<String, String> searchParameters = (requestEvent.getQueryStringParameters() == null
                    ? Map.of()
                    : requestEvent.getQueryStringParameters());

            var documentMetadataList = searchService.findByNhsNumberFromParameters(
                    searchParameters,
                    message -> logger.info(AUDIT, "{} searched for {}", userEmail, message));

            var fileOutputStream = new FileOutputStream("document.zip");
            var zipOutputStream = new ZipOutputStream(fileOutputStream);

            for (DocumentMetadata metadata : documentMetadataList) {
                try {
                    final ZipEntry entry = new ZipEntry(metadata.getDescription());
                    zipOutputStream.putNextEntry(entry);
                    IOUtils.copy(documentStore.getObjectFromS3(metadata), zipOutputStream);
                    zipOutputStream.closeEntry();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            return apiConfig.getApiGatewayResponse(200, null, "GET", null);
        } catch (Exception e) {
            return errorResponseGenerator.errorResponse(e, fhirContext.newJsonParser());
        }
    }

    private String getEmail(APIGatewayProxyRequestEvent requestEvent) {
        Map<String, String> headers = requestEvent.getHeaders();
        String authorizationHeader = headers.getOrDefault(
                "Authorization",
                headers.get("authorization"));
        if (authorizationHeader.isEmpty()) {
            logger.warn("Empty authorization header");
            return "[unknown]";
        }
        String token = authorizationHeader.replaceFirst("^[Bb]earer\\s+", "");
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getClaim("email").asString();
    }
}

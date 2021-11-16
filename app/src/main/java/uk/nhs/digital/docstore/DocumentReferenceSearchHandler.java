package uk.nhs.digital.docstore;

import ca.uhn.fhir.context.FhirContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.List;
import java.util.Map;

import static ca.uhn.fhir.context.PerformanceOptionsEnum.DEFERRED_MODEL_SCANNING;

@SuppressWarnings("unused")
public class DocumentReferenceSearchHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger = LoggerFactory.getLogger(DocumentReferenceSearchHandler.class);
    private static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");

    private final ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();
    private final BundleMapper bundleMapper = new BundleMapper();
    private final DocumentReferenceSearchService searchService;
    private final FhirContext fhirContext;

    public DocumentReferenceSearchHandler() {
        this.fhirContext = FhirContext.forR4();
        this.fhirContext.setPerformanceOptions(DEFERRED_MODEL_SCANNING);

        DocumentMetadataStore metadataStore = new DocumentMetadataStore();
        DocumentStore documentStore = new DocumentStore(System.getenv("DOCUMENT_STORE_BUCKET_NAME"));
        this.searchService = new DocumentReferenceSearchService(metadataStore, documentStore);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        var jsonParser = fhirContext.newJsonParser();

        String userEmail = getEmail(requestEvent);
        Bundle bundle;
        try {
            Map<String, String> searchParameters = (requestEvent.getQueryStringParameters() == null ? Map.of() : requestEvent.getQueryStringParameters());
            List<Document> documents = searchService.findByParameters(
                    searchParameters,
                    message -> logger.info(AUDIT, "{} searched for {}", userEmail, message));
            bundle = bundleMapper.toBundle(documents);
        } catch (Exception e) {
            logger.error("Unable to perform search", e);
            return errorResponseGenerator.errorResponse(e, jsonParser);
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(Map.of(
                        "Content-Type", "application/fhir+json",
                        "Access-Control-Allow-Origin", System.getenv("AMPLIFY_BASE_URL"),
                        "Access-Control-Allow-Methods", "GET"
                ))
                .withBody(jsonParser.encodeResourceToString(bundle));
    }

    private String getEmail(APIGatewayProxyRequestEvent requestEvent) {
        String authorizationHeader = requestEvent.getHeaders().get("authorization");
        String token = authorizationHeader.replaceFirst("^[Bb]earer\\s+", "");
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getClaim("email").asString();
    }
}

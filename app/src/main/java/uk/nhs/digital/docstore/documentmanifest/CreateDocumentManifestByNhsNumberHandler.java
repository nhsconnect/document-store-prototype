package uk.nhs.digital.docstore.documentmanifest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.PerformanceOptionsEnum;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import uk.nhs.digital.docstore.DocumentMetadata;
import uk.nhs.digital.docstore.DocumentMetadataStore;
import uk.nhs.digital.docstore.DocumentStore;
import uk.nhs.digital.docstore.ErrorResponseGenerator;
import uk.nhs.digital.docstore.common.DocumentMetadataSearchService;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.config.Tracer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("unused")
public class CreateDocumentManifestByNhsNumberHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger = LoggerFactory.getLogger(CreateDocumentManifestByNhsNumberHandler.class);
    private static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");

    private static final String SUBJECT_ID_CODING_SYSTEM = "https://fhir.nhs.uk/Id/nhs-number";
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

        try {
            var documentMetadataList = searchService.findByNhsNumberFromParameters(
                    requestEvent.getQueryStringParameters(), requestEvent.getHeaders());

            var zipInputStream = getPatientRecordAsZip(documentMetadataList);
            var documentName = "patient-record-"+UUID.randomUUID()+".zip";
            documentStore.addDocument(documentName, zipInputStream);

            var preSignedUrl = documentStore.generatePreSignedUrl(new DocumentStore.DocumentDescriptor(System.getenv("DOCUMENT_STORE_BUCKET_NAME"), documentName));

            return apiConfig.getApiGatewayResponse(200, preSignedUrl.toString(), "GET", null);
        } catch (Exception e) {
            return errorResponseGenerator.errorResponse(e, fhirContext.newJsonParser());
        }
    }

    private ByteArrayInputStream getPatientRecordAsZip(List<DocumentMetadata> documentMetadataList) throws IOException {
        var byteArrayOutputStream = new ByteArrayOutputStream();
        var zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        for (DocumentMetadata metadata : documentMetadataList) {
            if (metadata.isDocumentUploaded()){
                zipOutputStream.putNextEntry(new ZipEntry(metadata.getDescription()));

                IOUtils.copy(documentStore.getObjectFromS3(metadata), zipOutputStream);

                zipOutputStream.closeEntry();
            }
        }

        zipOutputStream.close();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
}

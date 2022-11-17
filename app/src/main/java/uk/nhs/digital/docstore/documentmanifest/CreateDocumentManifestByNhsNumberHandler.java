package uk.nhs.digital.docstore.documentmanifest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.PerformanceOptionsEnum;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DocumentManifest;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
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
import uk.nhs.digital.docstore.utils.CommonUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.stream.Collectors.toList;


@SuppressWarnings("unused")
public class CreateDocumentManifestByNhsNumberHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger = LoggerFactory.getLogger(CreateDocumentManifestByNhsNumberHandler.class);
    private static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");
    private static final String SUBJECT_ID_CODING_SYSTEM = "https://fhir.nhs.uk/Id/nhs-number";
    private static final String DOCUMENT_TYPE_CODING_SYSTEM = "http://snomed.info/sct";

    private final String bucketName = System.getenv("DOCUMENT_STORE_BUCKET_NAME");
    private final DocumentMetadataStore metadataStore = new DocumentMetadataStore();
    private final DocumentStore documentStore = new DocumentStore(bucketName);
    private final ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();
    private final FhirContext fhirContext;
    private final ApiConfig apiConfig;
    private final DocumentMetadataSearchService searchService = new DocumentMetadataSearchService(metadataStore);
    private final CommonUtils utils = new CommonUtils();

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

            var zipInputStream = getPatientRecordAsZip(documentMetadataList);

            var documentPath = CommonUtils.generateRandomUUIDString();
            var fileName = "patient-record-"+ nhsNumber +".zip";

            documentStore.addDocument(documentPath, zipInputStream);

            var descriptor = new DocumentStore.DocumentDescriptor(bucketName, documentPath);

            metadataStore.save(getDocumentMetadata(nhsNumber, fileName, descriptor.toLocation()));

            var preSignedUrl = documentStore.generatePreSignedUrlForZip(descriptor, fileName);

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

    private DocumentMetadata getDocumentMetadata(String nhsNumber, String documentName, String presignedUrl) {
        var documentMetadata = new DocumentMetadata();
        documentMetadata.setNhsNumber(nhsNumber);
        documentMetadata.setContentType("application/zip");
        documentMetadata.setLocation(presignedUrl);
        documentMetadata.setDocumentUploaded(true);
        documentMetadata.setDescription(documentName);
        documentMetadata.setCreated(Instant.now().toString());
        documentMetadata.setType(List.of("22151000087106"));
        documentMetadata.setIndexed(Instant.now().toString());
        return documentMetadata;
    }

    private DocumentManifest getDocumentManifest(DocumentMetadata documentMetadata, String presignedUrl) {
        var type = new CodeableConcept()
                .setCoding(documentMetadata.getType()
                        .stream()
                        .map(code -> new Coding()
                                .setCode(code)
                                .setSystem(DOCUMENT_TYPE_CODING_SYSTEM))
                        .collect(toList()));

        var documentManifest = new DocumentManifest()
                .setCreated(new Date(documentMetadata.getCreated()))
                .setSubject(new Reference()
                        .setIdentifier(new Identifier()
                                .setSystem(SUBJECT_ID_CODING_SYSTEM)
                                .setValue(documentMetadata.getNhsNumber())))
                .setSource(presignedUrl)
                .setType(type)
                .setStatus(Enumerations.DocumentReferenceStatus.CURRENT)
                .setDescription(documentMetadata.getDescription());

        return documentManifest;
    }
}

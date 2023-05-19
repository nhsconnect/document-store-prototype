package uk.nhs.digital.docstore.handlers;

import static java.util.stream.Collectors.toList;
import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.FINAL;
import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.PRELIMINARY;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.PerformanceOptionsEnum;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.util.Base64;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.ErrorResponseGenerator;
import uk.nhs.digital.docstore.NHSDocumentReference;
import uk.nhs.digital.docstore.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.config.Tracer;
import uk.nhs.digital.docstore.config.VirusScannerConfig;
import uk.nhs.digital.docstore.create.CreateDocumentReferenceRequestValidator;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.repository.DocumentStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.model.Document;
import uk.nhs.digital.docstore.model.DocumentLocation;
import uk.nhs.digital.docstore.services.DocumentReferenceService;
import uk.nhs.digital.docstore.utils.CommonUtils;

@SuppressWarnings("unused")
public class CreateDocumentReferenceHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(CreateDocumentReferenceHandler.class);
    private static final String DOCUMENT_TYPE_CODING_SYSTEM = "http://snomed.info/sct";
    private static final String SUBJECT_ID_CODING_SYSTEM = "https://fhir.nhs.uk/Id/nhs-number";
    private static final String AWS_REGION = "eu-west-2";
    private static final String DEFAULT_ENDPOINT = "";

    private final DocumentReferenceService documentReferenceService;
    private final FhirContext fhirContext;
    private final ApiConfig apiConfig;
    private final DocumentStore documentStore;
    private final VirusScannerConfig virusScannerConfig;

    private final ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();
    private final CreateDocumentReferenceRequestValidator requestValidator =
            new CreateDocumentReferenceRequestValidator();

    public CreateDocumentReferenceHandler() {
        this(
                new ApiConfig(),
                new DocumentReferenceService(
                        new DocumentMetadataStore(),
                        new SplunkPublisher(System.getenv("SQS_AUDIT_QUEUE_URL")),
                        new DocumentMetadataSerialiser()),
                new DocumentStore(),
                new VirusScannerConfig());
    }

    public CreateDocumentReferenceHandler(
            ApiConfig apiConfig,
            DocumentReferenceService documentReferenceService,
            DocumentStore documentStore,
            VirusScannerConfig virusScannerConfig) {
        this.apiConfig = apiConfig;
        this.documentReferenceService = documentReferenceService;
        this.virusScannerConfig = virusScannerConfig;
        this.fhirContext = FhirContext.forR4();
        this.fhirContext.setPerformanceOptions(PerformanceOptionsEnum.DEFERRED_MODEL_SCANNING);
        this.documentStore = documentStore;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {
        Document savedDocument;
        URL presignedS3Url;

        Tracer.setMDCContext(context);

        LOGGER.debug("API Gateway event received - processing starts");
        var jsonParser = fhirContext.newJsonParser();

        LOGGER.debug("TEST_API_KEY ciphertext: {}", System.getenv("TEST_API_KEY"));
        LOGGER.debug("TEST_API_KEY plain text: {}", decryptKey("TEST_API_KEY"));

        try {
            var inputDocumentReference =
                    jsonParser.parseResource(NHSDocumentReference.class, input.getBody());
            LOGGER.debug(
                    "Input document reference desc: " + inputDocumentReference.getDescription());
            LOGGER.debug(
                    "Input document reference  filename: " + inputDocumentReference.getFileName());
            requestValidator.validate(inputDocumentReference);

            var document = inputDocumentReference.parse();

            var s3BucketName = virusScannerConfig.getDocumentStoreBucketName();
            String s3ObjectKey = CommonUtils.generateRandomUUIDString();
            var s3Location = "s3://" + s3BucketName + "/" + s3ObjectKey;
            document.setLocation(new DocumentLocation(s3Location));

            LOGGER.debug("Saving DocumentReference to DynamoDB");
            presignedS3Url = documentStore.generatePreSignedUrlForDocument(document.getLocation());
            savedDocument = documentReferenceService.save(document);

            LOGGER.debug("Generating response body");
            var type =
                    new CodeableConcept()
                            .setCoding(
                                    savedDocument.getType().stream()
                                            .map(
                                                    code ->
                                                            new Coding()
                                                                    .setCode(code)
                                                                    .setSystem(
                                                                            DOCUMENT_TYPE_CODING_SYSTEM))
                                            .collect(toList()));

            var resource =
                    new NHSDocumentReference()
                            .setCreated(new DateTimeType(savedDocument.getCreated().toString()))
                            .setNhsNumber(savedDocument.getNhsNumber())
                            .setFileName(savedDocument.getFileName())
                            .addContent(
                                    new NHSDocumentReference.DocumentReferenceContentComponent()
                                            .setAttachment(
                                                    new Attachment()
                                                            .setUrl(presignedS3Url.toString())
                                                            .setContentType(
                                                                    savedDocument
                                                                            .getContentType())))
                            .setType(type)
                            .setDocStatus(savedDocument.isUploaded() ? FINAL : PRELIMINARY)
                            .setId(savedDocument.getReferenceId());

            var resourceAsJson = jsonParser.encodeResourceToString(resource);

            LOGGER.debug("Processing finished - about to return the response");
            return apiConfig.getApiGatewayResponse(
                    201,
                    resourceAsJson,
                    "POST",
                    "DocumentReference/" + savedDocument.getReferenceId());

        } catch (Exception e) {
            return errorResponseGenerator.errorResponse(e);
        }
    }

    private static String decryptKey(String envVariable) {
        System.out.println("Decrypting key");

        byte[] encryptedKey = Base64.decode(System.getenv(envVariable));
        Map<String, String> encryptionContext = new HashMap<>();
        encryptionContext.put("LambdaFunctionName", System.getenv("AWS_LAMBDA_FUNCTION_NAME"));

        AWSKMS client = AWSKMSClientBuilder.defaultClient();
        DecryptRequest request =
                new DecryptRequest()
                        .withCiphertextBlob(ByteBuffer.wrap(encryptedKey))
                        .withEncryptionContext(encryptionContext);
        ByteBuffer plainTextKey = client.decrypt(request).getPlaintext();

        return new String(plainTextKey.array(), StandardCharsets.UTF_8);
    }
}

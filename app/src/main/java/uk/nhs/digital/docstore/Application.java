package uk.nhs.digital.docstore;

import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.repository.DocumentZipTraceStore;
import uk.nhs.digital.docstore.filestorage.GeneratePresignedUrlRequestFactory;
import uk.nhs.digital.docstore.patientdetails.PdsFhirClient;
import uk.nhs.digital.docstore.patientdetails.RealPdsFhirClient;
import uk.nhs.digital.docstore.publishers.AuditPublisher;
import uk.nhs.digital.docstore.publishers.SplunkPublisher;

public class Application {
    public DocumentMetadataStore documentMetadataStore;

    public DocumentZipTraceStore documentZipTraceStore;

    public PdsFhirClient pdsFhirClient;

    public DocumentStore documentStore;

    public GeneratePresignedUrlRequestFactory generatePresignedUrlRequestFactory;
    public AuditPublisher auditPublisher;

    public Application () {
        var env = System.getenv();
        this.documentMetadataStore = new DocumentMetadataStore(env.getOrDefault("DYNAMODB_ENDPOINT", ""));
        this.documentZipTraceStore = new DocumentZipTraceStore(env.getOrDefault("DYNAMODB_ENDPOINT", ""));
        this.generatePresignedUrlRequestFactory = new GeneratePresignedUrlRequestFactory(env.get("DOCUMENT_STORE_BUCKET_NAME"));
        this.pdsFhirClient = new RealPdsFhirClient(env.get("PDS_FHIR_ENDPOINT"));
        this.documentStore = new DocumentStore(env.getOrDefault("S3_ENDPOINT", ""), "true".equals(System.getenv("S3_USE_PATH_STYLE")));
        this.auditPublisher = new SplunkPublisher();
    }
}

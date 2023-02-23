package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import uk.nhs.digital.docstore.audit.message.DownloadAllPatientRecordsAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.config.Tracer;
import uk.nhs.digital.docstore.data.entity.DocumentZipTrace;
import uk.nhs.digital.docstore.data.repository.DocumentStore;
import uk.nhs.digital.docstore.data.repository.DocumentZipTraceStore;
import uk.nhs.digital.docstore.model.Document;
import uk.nhs.digital.docstore.model.DocumentLocation;
import uk.nhs.digital.docstore.model.FileName;
import uk.nhs.digital.docstore.model.NhsNumber;
import uk.nhs.digital.docstore.utils.CommonUtils;

public class DocumentManifestService {
    private final AuditPublisher sensitiveIndex;
    private final DocumentZipTraceStore zipTraceStore;
    private final DocumentStore documentStore;
    private final String dbTimeToLive;

    public DocumentManifestService(
            AuditPublisher sensitiveIndex,
            DocumentZipTraceStore zipTraceStore,
            DocumentStore documentStore,
            String dbTimeToLive) {
        this.sensitiveIndex = sensitiveIndex;
        this.zipTraceStore = zipTraceStore;
        this.documentStore = documentStore;
        this.dbTimeToLive = dbTimeToLive;
    }

    public String saveZip(
            ByteArrayInputStream zipInputStream, List<Document> documentList, NhsNumber nhsNumber)
            throws JsonProcessingException {
        var documentPath = "tmp/" + CommonUtils.generateRandomUUIDString();
        var fileName = new FileName("patient-record-" + nhsNumber.getValue() + ".zip");

        var documentLocation = documentStore.addDocument(documentPath, zipInputStream);
        var documentZipTrace = createDocumentZipTrace(documentLocation);

        zipTraceStore.save(documentZipTrace);

        var preSignedUrl = documentStore.generatePreSignedUrlForZip(documentLocation, fileName);

        sensitiveIndex.publish(new DownloadAllPatientRecordsAuditMessage(nhsNumber, documentList));

        return preSignedUrl.toString();
    }

    private DocumentZipTrace createDocumentZipTrace(DocumentLocation location) {
        long timeToLive = Long.parseLong(dbTimeToLive);
        var expDate = Instant.now().plus(timeToLive, ChronoUnit.DAYS);
        var documentZipTrace = new DocumentZipTrace();
        documentZipTrace.setCorrelationId(Tracer.getCorrelationId());
        documentZipTrace.setCreatedAt(Instant.now().toString());
        documentZipTrace.setLocation(location.toString());
        documentZipTrace.setExpiryDate(expDate.getEpochSecond());

        return documentZipTrace;
    }
}

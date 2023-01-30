package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import uk.nhs.digital.docstore.audit.message.DownloadAllPatientRecordsAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.model.Document;
import uk.nhs.digital.docstore.model.NhsNumber;

public class DocumentManifestService {
    private final AuditPublisher sensitiveIndex;

    public DocumentManifestService(AuditPublisher sensitiveIndex) {
        this.sensitiveIndex = sensitiveIndex;
    }

    public void audit(NhsNumber nhsNumber, List<Document> documentList)
            throws JsonProcessingException {
        sensitiveIndex.publish(new DownloadAllPatientRecordsAuditMessage(nhsNumber, documentList));
    }
}

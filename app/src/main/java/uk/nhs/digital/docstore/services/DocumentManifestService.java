package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.nhs.digital.docstore.auditmessages.DownloadAllPatientRecordsAuditMessage;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.publishers.AuditPublisher;

import java.util.List;

public class DocumentManifestService {
    private final AuditPublisher sensitiveIndex;

    public DocumentManifestService(AuditPublisher sensitiveIndex) {
        this.sensitiveIndex = sensitiveIndex;
    }

    public void audit(String nhsNumber, List<DocumentMetadata> documentMetadataList) throws JsonProcessingException {
        sensitiveIndex.publish(new DownloadAllPatientRecordsAuditMessage(nhsNumber, documentMetadataList));
    }
}

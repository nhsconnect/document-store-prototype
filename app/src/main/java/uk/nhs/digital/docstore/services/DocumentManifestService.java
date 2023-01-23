package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.nhs.digital.docstore.audit.message.DownloadAllPatientRecordsAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.model.NhsNumber;

import java.util.List;

public class DocumentManifestService {
    private final AuditPublisher sensitiveIndex;

    public DocumentManifestService(AuditPublisher sensitiveIndex) {
        this.sensitiveIndex = sensitiveIndex;
    }

    public void audit(NhsNumber nhsNumber, List<DocumentMetadata> documentMetadataList) throws JsonProcessingException {
        sensitiveIndex.publish(new DownloadAllPatientRecordsAuditMessage(nhsNumber, documentMetadataList));
    }
}

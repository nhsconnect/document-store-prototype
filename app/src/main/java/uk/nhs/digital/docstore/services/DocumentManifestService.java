package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.nhs.digital.docstore.auditmessages.DownloadAllPatientRecordsAuditMessage;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.publishers.SplunkPublisher;

import java.util.List;

public class DocumentManifestService {
    private final SplunkPublisher publisher;

    public DocumentManifestService(SplunkPublisher publisher) {
        this.publisher = publisher;
    }

    public void audit(String nhsNumber, List<DocumentMetadata> documentMetadataList) throws JsonProcessingException {
        publisher.publish(new DownloadAllPatientRecordsAuditMessage(nhsNumber, documentMetadataList));
    }
}

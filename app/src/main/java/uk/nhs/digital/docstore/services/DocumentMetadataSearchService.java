package uk.nhs.digital.docstore.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.documentmanifest.CreateDocumentManifestByNhsNumberHandler;
import uk.nhs.digital.docstore.model.NhsNumber;

import java.util.List;


public class DocumentMetadataSearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateDocumentManifestByNhsNumberHandler.class);
    private final DocumentMetadataStore metadataStore;

    public DocumentMetadataSearchService(DocumentMetadataStore metadataStore) {
        this.metadataStore = metadataStore;
    }

    public List<DocumentMetadata> findMetadataByNhsNumber(NhsNumber nhsNumber) {
        LOGGER.info("Searched for documents with NHS number " + nhsNumber);
        return metadataStore.findByNhsNumber(nhsNumber);
    }
}

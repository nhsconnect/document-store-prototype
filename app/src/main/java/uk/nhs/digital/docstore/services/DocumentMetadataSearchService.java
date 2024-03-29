package uk.nhs.digital.docstore.services;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.Document;
import uk.nhs.digital.docstore.model.NhsNumber;

public class DocumentMetadataSearchService {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(DocumentMetadataSearchService.class);
    private final DocumentMetadataStore metadataStore;
    private final DocumentMetadataSerialiser serialiser;

    public DocumentMetadataSearchService(
            DocumentMetadataStore metadataStore, DocumentMetadataSerialiser serialiser) {
        this.metadataStore = metadataStore;
        this.serialiser = serialiser;
    }

    public List<Document> findMetadataByNhsNumber(NhsNumber nhsNumber)
            throws IllFormedPatientDetailsException {
        LOGGER.info("Searched for documents with NHS number " + nhsNumber);
        List<Document> documentList = new ArrayList<>();
        for (DocumentMetadata documentMetadata : metadataStore.findByNhsNumber(nhsNumber)) {
            Document document = serialiser.toDocumentModel(documentMetadata);
            documentList.add(document);
        }
        return documentList;
    }
}

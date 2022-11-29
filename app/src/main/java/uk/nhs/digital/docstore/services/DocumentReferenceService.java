package uk.nhs.digital.docstore.services;

import uk.nhs.digital.docstore.NHSDocumentReference;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;

public class DocumentReferenceService {


    private final DocumentMetadataStore store;
    private final String bucketName;

    public DocumentReferenceService(DocumentMetadataStore store, String bucketName) {
        this.store = store;
        this.bucketName = bucketName;
    }

    public DocumentMetadata save(NHSDocumentReference documentReference, String s3ObjectKey) {
        var s3Location = "s3://" + bucketName + "/" + s3ObjectKey;
        var documentMetadata = DocumentMetadata.from(documentReference, s3Location);
        return store.save(documentMetadata);
    }
}

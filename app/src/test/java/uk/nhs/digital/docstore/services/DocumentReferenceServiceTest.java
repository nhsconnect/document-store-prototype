package uk.nhs.digital.docstore.services;

import org.hl7.fhir.r4.model.DocumentReference;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.NHSDocumentReference;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.*;

public class DocumentReferenceServiceTest {
    @Test
    public void savesDocumentMetadata() {
        var documentMetadataStore = mock(DocumentMetadataStore.class);
        var s3ObjectKey = "key";
        var bucketName = "bucket";

        var documentReference = new NHSDocumentReference();
        documentReference.setContent(List.of(new DocumentReference.DocumentReferenceContentComponent()));

        String documentLocation = "s3://" + bucketName + "/" + s3ObjectKey;
        var stubDocumentMetadata = DocumentMetadata.from(documentReference, documentLocation);
        var documentReferenceService = new DocumentReferenceService(documentMetadataStore, bucketName);
        when(documentMetadataStore.save(any(DocumentMetadata.class))).thenReturn(stubDocumentMetadata);

        var actualDocumentMetadata = documentReferenceService.save(documentReference, s3ObjectKey);

        verify(documentMetadataStore, times(1)).save(any(DocumentMetadata.class));

        assertThat(actualDocumentMetadata).isEqualTo(stubDocumentMetadata);
    }
}

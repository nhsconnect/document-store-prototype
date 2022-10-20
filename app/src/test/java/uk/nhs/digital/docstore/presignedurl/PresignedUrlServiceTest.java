package uk.nhs.digital.docstore.presignedurl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.Document;
import uk.nhs.digital.docstore.DocumentMetadataStore;
import uk.nhs.digital.docstore.DocumentStore;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.nhs.digital.docstore.DocumentMetadataBuilder.theMetadata;

@ExtendWith(MockitoExtension.class)
class PresignedUrlServiceTest {

    private PresignedUrlService presignedUrlService;

    @Mock
    private DocumentMetadataStore metadataStore;
    @Mock
    private DocumentStore documentStore;

    @BeforeEach
    void setUp() {
        presignedUrlService = new PresignedUrlService(metadataStore, documentStore);
    }

    @Test
    void supportsDifferentSearchSyntaxes() throws MalformedURLException {
        String id = "1234";
        var metadataTemplate = theMetadata()
                .withId(id)
                .withDocumentUploaded(true);
        when(metadataStore.getById(id))
                .thenReturn(metadataTemplate.build());
        when(documentStore.generatePreSignedUrl(any()))
                .thenReturn(new URL("https://example.org/"));

        var actualDocument = presignedUrlService.findByParameters(Map.of("id", id));
        var expectedDocument = new Document(metadataTemplate.build(), new URL("https://example.org/"));

        assertThat(actualDocument.getPreSignedUrl()).isEqualTo(expectedDocument.getPreSignedUrl());
    }

    @Test
    void omitsPreSignedUrlIfDocumentIsNotAvailable() {
        String id = "1234";
        var metadataTemplate = theMetadata()
                .withId(id)
                .withDocumentUploaded(false);
        when(metadataStore.getById(id))
                .thenReturn(metadataTemplate.build());

        var actualDocument = presignedUrlService.findByParameters(Map.of("id", id));

        assertThat(actualDocument.getPreSignedUrl()).isNull();
        verify(documentStore, never()).generatePreSignedUrl(any());
    }

}
package uk.nhs.digital.docstore.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.exceptions.IllFormedPatentDetailsException;
import uk.nhs.digital.docstore.logs.TestLogAppender;
import uk.nhs.digital.docstore.model.NhsNumber;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.nhs.digital.docstore.helpers.DocumentMetadataBuilder.theMetadata;

@ExtendWith(MockitoExtension.class)
class DocumentMetadataSearchServiceTest {
    private static final String JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2V4YW1wbGUuYXV0aDAuY29tLyIsImF1ZCI6Imh0dHBzOi8vYXBpLmV4YW1wbGUuY29tL2NhbGFuZGFyL3YxLyIsInN1YiI6InVzcl8xMjMiLCJpYXQiOjE0NTg3ODU3OTYsImV4cCI6MTQ1ODg3MjE5Nn0.CA7eaHjIHz5NxeIJoFK9krqaeZrPLwmMmgI_XiQiIkQ";

    @Mock
    private DocumentMetadataStore metadataStore;
    
    private DocumentMetadataSearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new DocumentMetadataSearchService(metadataStore);
    }

    @Test
    void supportsDifferentIdentifierSyntax() throws IllFormedPatentDetailsException {
        var nhsNumber = new NhsNumber("1234567890");
        var metadataTemplate = theMetadata().withNhsNumber(nhsNumber).withDocumentUploaded(true);
        
        when(metadataStore.findByNhsNumber(nhsNumber)).thenReturn(List.of(metadataTemplate.build()));
        List<DocumentMetadata> documents = searchService.findMetadataByNhsNumber(nhsNumber);

        assertThat(documents)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(metadataTemplate.build());
    }

    @Test
    void supportsDifferentSearchSyntax() throws IllFormedPatentDetailsException {
        var nhsNumber = new NhsNumber("9000000009");
        var metadataTemplate = theMetadata().withNhsNumber(nhsNumber).withDocumentUploaded(true);
        
        when(metadataStore.findByNhsNumber(nhsNumber)).thenReturn(List.of(metadataTemplate.build()));
        List<DocumentMetadata> documents = searchService.findMetadataByNhsNumber(nhsNumber);

        assertThat(documents)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(metadataTemplate.build());
    }

    @Test
    void omitsPreSignedUrlIfDocumentIsNotAvailable() throws IllFormedPatentDetailsException {
        var nhsNumber = new NhsNumber("9000000009");
        var metadataTemplate = theMetadata().withNhsNumber(nhsNumber).withDocumentUploaded(false);
        
        when(metadataStore.findByNhsNumber(nhsNumber)).thenReturn(List.of(metadataTemplate.build()));
        List<DocumentMetadata> documents = searchService.findMetadataByNhsNumber(nhsNumber);

        assertThat(documents)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(metadataTemplate.build());
    }

    @Test
    void logsTheSearchActionObfuscatingPii() throws IllFormedPatentDetailsException {
        var testLogAppender = TestLogAppender.addTestLogAppender();
        var nhsNumber = new NhsNumber("1234567890");
        
        when(metadataStore.findByNhsNumber(nhsNumber)).thenReturn(List.of());
        searchService.findMetadataByNhsNumber(nhsNumber);

        assertThat(testLogAppender.findLoggedEvent("Searched for documents with NHS number 123 *** ****")).isNotNull();
    }
}

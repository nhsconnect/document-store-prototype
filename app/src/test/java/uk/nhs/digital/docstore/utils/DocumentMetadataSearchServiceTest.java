package uk.nhs.digital.docstore.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.DocumentMetadata;
import uk.nhs.digital.docstore.DocumentMetadataStore;
import uk.nhs.digital.docstore.logs.TestLogAppender;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.nhs.digital.docstore.DocumentMetadataBuilder.theMetadata;

@ExtendWith(MockitoExtension.class)
class DocumentMetadataSearchServiceTest {
    private static final String JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2V4YW1wbGUuYXV0aDAuY29tLyIsImF1ZCI6Imh0dHBzOi8vYXBpLmV4YW1wbGUuY29tL2NhbGFuZGFyL3YxLyIsInN1YiI6InVzcl8xMjMiLCJpYXQiOjE0NTg3ODU3OTYsImV4cCI6MTQ1ODg3MjE5Nn0.CA7eaHjIHz5NxeIJoFK9krqaeZrPLwmMmgI_XiQiIkQ";

    private DocumentMetadataSearchService searchService;

    @Mock
    private DocumentMetadataStore metadataStore;

    private Map<String, String> headers = Map.of("Authorization", "Bearer " + JWT);

    @BeforeEach
    void setUp() {
        searchService = new DocumentMetadataSearchService(metadataStore);
    }

    @Test
    void supportsDifferentIdentifierSyntaxes() {
        String nhsNumber = "1234567890";
        var metadataTemplate = theMetadata()
                .withNhsNumber(nhsNumber)
                .withDocumentUploaded(true);
        when(metadataStore.findByNhsNumber(nhsNumber))
                .thenReturn(List.of(metadataTemplate.build()));

        List<DocumentMetadata> documents = searchService.findMetadataByNhsNumber(nhsNumber, headers);

        assertThat(documents)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(metadataTemplate.build());
    }

    @Test
    void supportsDifferentSearchSyntaxes() {
        String nhsNumber = "9000000009";
        var metadataTemplate = theMetadata()
                .withNhsNumber(nhsNumber)
                .withDocumentUploaded(true);
        when(metadataStore.findByNhsNumber(nhsNumber))
                .thenReturn(List.of(metadataTemplate.build()));

        List<DocumentMetadata> documents = searchService.findMetadataByNhsNumber(nhsNumber, headers);

        assertThat(documents)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(metadataTemplate.build());
    }

    @Test
    void omitsPreSignedUrlIfDocumentIsNotAvailable() {
        String nhsNumber = "9000000009";
        var metadataTemplate = theMetadata()
                .withNhsNumber(nhsNumber)
                .withDocumentUploaded(false);
        when(metadataStore.findByNhsNumber(nhsNumber))
                .thenReturn(List.of(metadataTemplate.build()));

        List<DocumentMetadata> documents = searchService.findMetadataByNhsNumber(nhsNumber, headers);

        assertThat(documents)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(metadataTemplate.build());
    }

    @Test
    void logsTheSearchActionObfuscatingPii() {
        var testLogAppender = TestLogAppender.addTestLogAppender();
        String nhsNumber = "1234567890";
        when(metadataStore.findByNhsNumber(nhsNumber))
                .thenReturn(List.of());

        searchService.findMetadataByNhsNumber(nhsNumber, headers);

        assertThat(testLogAppender.findLoggedEvent("documents with NHS number ending 7890")).isNotNull();
    }
}

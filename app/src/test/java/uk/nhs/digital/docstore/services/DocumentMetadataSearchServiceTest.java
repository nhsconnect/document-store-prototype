package uk.nhs.digital.docstore.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.nhs.digital.docstore.helpers.DocumentMetadataBuilder.theMetadata;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.helpers.DocumentBuilder;
import uk.nhs.digital.docstore.logs.TestLogAppender;
import uk.nhs.digital.docstore.model.Document;
import uk.nhs.digital.docstore.model.NhsNumber;

@ExtendWith(MockitoExtension.class)
class DocumentMetadataSearchServiceTest {
  private static final String JWT =
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2V4YW1wbGUuYXV0aDAuY29tLyIsImF1ZCI6Imh0dHBzOi8vYXBpLmV4YW1wbGUuY29tL2NhbGFuZGFyL3YxLyIsInN1YiI6InVzcl8xMjMiLCJpYXQiOjE0NTg3ODU3OTYsImV4cCI6MTQ1ODg3MjE5Nn0.CA7eaHjIHz5NxeIJoFK9krqaeZrPLwmMmgI_XiQiIkQ";

  @Mock private DocumentMetadataStore metadataStore;
  @Mock private DocumentMetadataSerialiser serialiser;

  private DocumentMetadataSearchService searchService;

  @BeforeEach
  void setUp() {
    searchService = new DocumentMetadataSearchService(metadataStore, serialiser);
  }

  @Test
  void findMatchingDocumentMetadataObjectsAndSerialisesToDocuments()
      throws IllFormedPatientDetailsException {
    var nhsNumber = new NhsNumber("1234567890");
    var metadata = theMetadata().withNhsNumber(nhsNumber).withDocumentUploaded(true).build();
    var document = DocumentBuilder.baseDocumentBuilder().build();

    when(metadataStore.findByNhsNumber(nhsNumber)).thenReturn(List.of(metadata));
    when(serialiser.toDocumentModel(metadata)).thenReturn(document);

    List<Document> documents = searchService.findMetadataByNhsNumber(nhsNumber);

    assertThat(documents.get(0)).isEqualTo(document);
  }

  @Test
  void logsTheSearchActionObfuscatingPii() throws IllFormedPatientDetailsException {
    var testLogAppender = TestLogAppender.addTestLogAppender();
    var nhsNumber = new NhsNumber("1234567890");

    when(metadataStore.findByNhsNumber(nhsNumber)).thenReturn(List.of());
    searchService.findMetadataByNhsNumber(nhsNumber);

    assertThat(
            testLogAppender.findLoggedEvent("Searched for documents with NHS number 123 *** ****"))
        .isNotNull();
  }
}

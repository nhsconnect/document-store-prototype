package uk.nhs.digital.docstore.data.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.nhs.digital.docstore.helpers.DocumentMetadataBuilder.theMetadata;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.NhsNumber;

@ExtendWith(MockitoExtension.class)
class DocumentMetadataStoreTest {
  @Mock private DynamoDBMapper dynamoDBMapper;
  @Mock private PaginatedQueryList<DocumentMetadata> documentMetadataPaginatedQueryList;

  private DocumentMetadataStore documentMetadataStore;

  @BeforeEach
  void setUp() {
    documentMetadataStore = new DocumentMetadataStore(dynamoDBMapper);
  }

  @Test
  void returnsDocumentReferenceWithMatchingId() throws IllFormedPatientDetailsException {
    var id = "1234";
    var documentMetadata = theMetadata().withId(id).build();

    when(dynamoDBMapper.load(DocumentMetadata.class, id)).thenReturn(documentMetadata);
    var documentReference = documentMetadataStore.getById(id);

    assertThat(documentReference.getId()).isEqualTo(id);
  }

  @Test
  void returnsNonDeletedDocumentMetadataListByNhsNumber() throws IllFormedPatientDetailsException {
    var nhsNumber = new NhsNumber("9000000009");
    var documentMetadata = theMetadata().withNhsNumber(nhsNumber).withDeleted(null).build();
    var softDeletedDocumentMetadata =
        theMetadata().withNhsNumber(nhsNumber).withDeleted("2023-01-17T09:45:59.457620Z").build();
    var documentMetadataList = List.of(documentMetadata, softDeletedDocumentMetadata);

    when(dynamoDBMapper.query(eq(DocumentMetadata.class), any()))
        .thenReturn(documentMetadataPaginatedQueryList);
    when(documentMetadataPaginatedQueryList.stream()).thenReturn(documentMetadataList.stream());
    var actualDocumentMetadataList = documentMetadataStore.findByNhsNumber(nhsNumber);

    assertThat(actualDocumentMetadataList).doesNotContain(softDeletedDocumentMetadata);
  }
}

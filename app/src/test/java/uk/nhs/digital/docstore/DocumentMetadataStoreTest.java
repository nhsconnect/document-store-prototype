package uk.nhs.digital.docstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class DocumentMetadataStoreTest {
    private DocumentMetadataStore store;

    @BeforeEach
    void setUp() {
        store = new DocumentMetadataStore();
    }

    @Test
    void returnsDocumentReferenceWithMatchingId() {
        var documentReference = store.getById("1234");

        assertThat(documentReference.getId()).isEqualTo("1234");
    }

    @Test
    void returnsDifferentDocumentReferencesForDifferentIds() {
        var documentReference = store.getById("5678");

        assertThat(documentReference.getId()).isEqualTo("5678");
    }
}

package uk.nhs.digital.docstore.search;

import uk.nhs.digital.docstore.Document;
import uk.nhs.digital.docstore.DocumentMetadataStore;
import uk.nhs.digital.docstore.NHSNumberSearchParameterForm;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

class DocumentReferenceSearchService {

    private final DocumentMetadataStore metadataStore;

    public DocumentReferenceSearchService(DocumentMetadataStore metadataStore) {
        this.metadataStore = metadataStore;
    }

    public List<Document> findByParameters(Map<String, String> parameters, Consumer<String> logger) {
        String nhsNumber = getNhsNumberFrom(parameters);
        logger.accept("documents with NHS number ending " + obfuscate(nhsNumber));
        return metadataStore.findByNhsNumber(nhsNumber)
                .stream()
                .map(metadata -> new Document(metadata))
                .collect(toList());
    }

    private String getNhsNumberFrom(Map<String, String> queryParameters) {
        NHSNumberSearchParameterForm nhsNumberSearchParameterForm = new NHSNumberSearchParameterForm(queryParameters);
        return nhsNumberSearchParameterForm.getNhsNumber();
    }

    private String obfuscate(String string) {
        return string.substring(string.length() - 4);
    }
}

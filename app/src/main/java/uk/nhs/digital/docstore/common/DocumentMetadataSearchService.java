package uk.nhs.digital.docstore.common;

import uk.nhs.digital.docstore.DocumentMetadata;
import uk.nhs.digital.docstore.DocumentMetadataStore;
import uk.nhs.digital.docstore.NHSNumberSearchParameterForm;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DocumentMetadataSearchService {

    private final DocumentMetadataStore metadataStore;

    public DocumentMetadataSearchService(DocumentMetadataStore metadataStore) {
        this.metadataStore = metadataStore;
    }

    public List<DocumentMetadata> findByNhsNumberFromParameters(Map<String, String> parameters, Consumer<String> logger) {
        String nhsNumber = getNhsNumberFrom(parameters);
        logger.accept("documents with NHS number ending " + obfuscate(nhsNumber));
        return metadataStore.findByNhsNumber(nhsNumber);
    }

    private String getNhsNumberFrom(Map<String, String> queryParameters) {
        NHSNumberSearchParameterForm nhsNumberSearchParameterForm = new NHSNumberSearchParameterForm(queryParameters);
        return nhsNumberSearchParameterForm.getNhsNumber();
    }

    private String obfuscate(String string) {
        return string.substring(string.length() - 4);
    }
}

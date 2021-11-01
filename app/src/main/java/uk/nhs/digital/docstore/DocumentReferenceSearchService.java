package uk.nhs.digital.docstore;

import uk.nhs.digital.docstore.DocumentStore.DocumentDescriptor;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

public class DocumentReferenceSearchService {
    private static final Pattern SUBJECT_IDENTIFIER_PATTERN = Pattern.compile("^(?<systempart>(?<system>.*?)(?<!\\\\)\\|)?(?<identifier>.*)$");
    private static final String NHS_NUMBER_SYSTEM_ID = "https://fhir.nhs.uk/Id/nhs-number";

    private final DocumentMetadataStore metadataStore;
    private final DocumentStore documentStore;

    public DocumentReferenceSearchService(DocumentMetadataStore metadataStore, DocumentStore documentStore) {
        this.metadataStore = metadataStore;
        this.documentStore = documentStore;
    }

    public List<Document> findByParameters(Map<String, String> parameters) {
        String nhsNumber = getNhsNumberFrom(parameters);
        return metadataStore.findByNhsNumber(nhsNumber)
                .stream()
                .map(metadata -> new Document(metadata, getPreSignedUrl(metadata)))
                .collect(toList());
    }

    private String getNhsNumberFrom(Map<String, String> queryParameters) {
        String subject = Optional.ofNullable(queryParameters.get("subject:identifier"))
                .or(() -> Optional.ofNullable(queryParameters.get("subject.identifier")))
                .orElseThrow(() -> new MissingSearchParametersException("subject:identifier"));

        return validSubject(subject);
    }

    private URL getPreSignedUrl(DocumentMetadata metadata) {
        if (!metadata.isDocumentUploaded()) {
            return null;
        }

        var descriptor = DocumentDescriptor.from(metadata);
        return documentStore.generatePreSignedUrl(descriptor);
    }

    private String validSubject(String subject) {
        Matcher matcher = SUBJECT_IDENTIFIER_PATTERN.matcher(subject);
        if (!matcher.matches() || matcher.group("identifier").isBlank()) {
            throw new InvalidSubjectIdentifierException(subject);
        }
        if (matcher.group("systempart") != null && !NHS_NUMBER_SYSTEM_ID.equals(matcher.group("system"))) {
            throw new UnrecognisedSubjectIdentifierSystemException(matcher.group("system"));
        }

        return matcher.group("identifier");
    }
}

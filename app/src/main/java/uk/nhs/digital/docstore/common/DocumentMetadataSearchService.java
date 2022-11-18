package uk.nhs.digital.docstore.common;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.DocumentMetadata;
import uk.nhs.digital.docstore.DocumentMetadataStore;
import uk.nhs.digital.docstore.documentmanifest.CreateDocumentManifestByNhsNumberHandler;

import java.util.List;
import java.util.Map;


public class DocumentMetadataSearchService {
    private static final Logger logger = LoggerFactory.getLogger(CreateDocumentManifestByNhsNumberHandler.class);
    private final DocumentMetadataStore metadataStore;

    public DocumentMetadataSearchService(DocumentMetadataStore metadataStore) {
        this.metadataStore = metadataStore;
    }

    public List<DocumentMetadata> findMetadataByNhsNumber(String nhsNumber, Map<String, String> requestHeaders) {
        var userEmail = getEmail(requestHeaders);
        logger.info(userEmail + "searched for documents with NHS number ending " + obfuscate(nhsNumber));
        return metadataStore.findByNhsNumber(nhsNumber);
    }

    private String obfuscate(String string) {
        return string.substring(string.length() - 4);
    }

    private String getEmail(Map<String, String> headers) {
        String authorizationHeader = headers.getOrDefault("Authorization", headers.get("authorization"));

        if (authorizationHeader.isEmpty()) {
            logger.warn("Empty authorization header");
            return "[unknown]";
        }
        String token = authorizationHeader.replaceFirst("^[Bb]earer\\s+", "");
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getClaim("email").asString();
    }
}
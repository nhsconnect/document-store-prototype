package uk.nhs.digital.docstore;

import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

import java.util.HashMap;
import java.util.Map;

public class DocumentReferenceStore {
    private final Map<String, DocumentReference> referencesById = new HashMap<>();

    public DocumentReferenceStore() {
        referencesById.put("1234", (DocumentReference) new DocumentReference()
                .setSubject(new Reference()
                        .setIdentifier(new Identifier()
                                .setSystem("https://fhir.nhs.uk/Id/nhs-number")
                                .setValue("12345")))
                .setId("1234"));
        referencesById.put("5678", (DocumentReference) new DocumentReference()
                .setSubject(new Reference()
                        .setIdentifier(new Identifier()
                                .setSystem("https://fhir.nhs.uk/Id/nhs-number")
                                .setValue("56789")))
                .setId("5678"));
    }

    public DocumentReference getById(String id) {
        return referencesById.get(id);
    }
}

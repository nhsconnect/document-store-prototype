package uk.nhs.digital.docstore.search;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Reference;
import uk.nhs.digital.docstore.Document;
import uk.nhs.digital.docstore.NHSDocumentReference;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hl7.fhir.r4.model.Bundle.BundleType.SEARCHSET;
import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.FINAL;
import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.PRELIMINARY;

class BundleMapper {
    private static final String NHS_NUMBER_SYSTEM_ID = "https://fhir.nhs.uk/Id/nhs-number";
    private static final String DOCUMENT_TYPE_CODING_SYSTEM = "http://snomed.info/sct";

    public Bundle toBundle(List<Document> documents) {
        var entries = documents.stream()
                .map(this::toBundleEntries)
                .collect(toList());

        return new Bundle()
                .setTotal(entries.size())
                .setType(SEARCHSET)
                .setEntry(entries);
    }

    private BundleEntryComponent toBundleEntries(Document document) {
        return new BundleEntryComponent()
                .setFullUrl("/DocumentReference/" + document.getReferenceId())
                .setResource(toDocumentReference(document));
    }

    private DocumentReference toDocumentReference(Document document) {

        var type = new CodeableConcept()
                .setCoding(document.getType()
                        .stream()
                        .map(code -> new Coding()
                                .setCode(code)
                                .setSystem(DOCUMENT_TYPE_CODING_SYSTEM))
                        .collect(toList()));

        return (DocumentReference) new NHSDocumentReference()
                .setCreated(new DateTimeType(document.getCreated()))
                .setIndexed(new InstantType(document.getIndexed()))
                .setSubject(new Reference()
                        .setIdentifier(new Identifier()
                                .setSystem(NHS_NUMBER_SYSTEM_ID)
                                .setValue(document.getNhsNumber())))
                .setType(type)
                .setDocStatus(document.isUploaded() ? FINAL : PRELIMINARY)
                .setDescription(document.getDescription())
                .setId(document.getReferenceId());
    }
}

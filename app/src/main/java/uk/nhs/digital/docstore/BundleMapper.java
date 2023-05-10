package uk.nhs.digital.docstore;

import static java.util.stream.Collectors.toList;
import static org.hl7.fhir.r4.model.Bundle.BundleType.SEARCHSET;
import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.FINAL;
import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.PRELIMINARY;

import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.InstantType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.handlers.DocumentReferenceSearchHandler;
import uk.nhs.digital.docstore.model.Document;

public class BundleMapper {
    private static final String DOCUMENT_TYPE_CODING_SYSTEM = "http://snomed.info/sct";
    private static final Logger LOGGER =
            LoggerFactory.getLogger(DocumentReferenceSearchHandler.class);

    public Bundle toBundle(List<Document> documents) throws IllFormedPatientDetailsException {
        var entries = new ArrayList<BundleEntryComponent>();
        for (Document document : documents) {
            BundleEntryComponent bundleEntryComponent = toBundleEntries(document);
            entries.add(bundleEntryComponent);
        }

        return new Bundle().setTotal(entries.size()).setType(SEARCHSET).setEntry(entries);
    }

    private BundleEntryComponent toBundleEntries(Document document)
            throws IllFormedPatientDetailsException {
        return new BundleEntryComponent()
                .setFullUrl("/DocumentReference/" + document.getReferenceId())
                .setResource(toDocumentReference(document));
    }

    private DocumentReference toDocumentReference(Document document)
            throws IllFormedPatientDetailsException {

        var type =
                new CodeableConcept()
                        .setCoding(
                                document.getType().stream()
                                        .map(
                                                code ->
                                                        new Coding()
                                                                .setCode(code)
                                                                .setSystem(
                                                                        DOCUMENT_TYPE_CODING_SYSTEM))
                                        .collect(toList()));
        var isUploaded = document.isUploaded();

        LOGGER.debug("///ISUPLOADED/// = " + (isUploaded ? "true" : "false"));
        return (DocumentReference)
                new NHSDocumentReference()
                        .setCreated(new DateTimeType(document.getCreated().toString()))
                        .setIndexed(
                                isUploaded
                                        ? new InstantType(document.getIndexed().toString())
                                        : null)
                        .setVirusScanResult(document.getVirusScanResult().toString())
                        .setNhsNumber(document.getNhsNumber())
                        .setFileName(document.getFileName())
                        .setType(type)
                        .setDocStatus(document.isUploaded() ? FINAL : PRELIMINARY)
                        .setId(document.getReferenceId());
    }
}

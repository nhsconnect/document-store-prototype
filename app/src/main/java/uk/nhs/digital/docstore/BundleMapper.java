package uk.nhs.digital.docstore;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.InstantType;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.Document;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hl7.fhir.r4.model.Bundle.BundleType.SEARCHSET;
import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.FINAL;
import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.PRELIMINARY;

public class BundleMapper {
  private static final String DOCUMENT_TYPE_CODING_SYSTEM = "http://snomed.info/sct";

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
                    .map(code -> new Coding().setCode(code).setSystem(DOCUMENT_TYPE_CODING_SYSTEM))
                    .collect(toList()));

    return (DocumentReference)
        new NHSDocumentReference()
            .setCreated(new DateTimeType(document.getCreated().toString()))
            .setIndexed(
                document.isUploaded() ? new InstantType(document.getIndexed().toString()) : null)
            .setNhsNumber(document.getNhsNumber())
            .setFileName(document.getFileName())
            .setType(type)
            .setDocStatus(document.isUploaded() ? FINAL : PRELIMINARY)
            .setId(document.getReferenceId());
  }
}
